import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;

public class Shooter extends JFrame {
    int screenMaxWidth;
    int screenMaxHeight;
    int cameraX;
    int cameraY;
    int cameraWidth = 500;
    int cameraHeight = 300;
    int shooterX = 0;
    int shooterY = 0;
    final int shooterSize = 40;
    int mouseCursorX = 0;
    int mouseCursorY = 0;
    double shooterAimAngle = 0;
    int shooterXVec = 0;
    int shooterYVec = 0;
    int windowPYSizeVec = 0;
    int windowPXSizeVec = 0;
    int windowNYSizeVec = 0;
    int windowNXSizeVec = 0;
    Thread requestFocusThread = new Thread(() -> {
        while (true) {
            requestFocus();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
    JPanel shooterScreenPanel = new JPanel() {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            //draw shooter
            g2d.setColor(Color.white);
            g2d.fillOval(shooterX - cameraX, shooterY - cameraY, shooterSize, shooterSize);
            g2d.setColor(Color.black);
            g2d.fillOval(shooterX - cameraX + 1, shooterY - cameraY + 1, shooterSize-2, shooterSize-2);

            //draw shooter aim
            final int aimArrowSize = 100;
            final int aimArrowX = (int)(shooterX - cameraX + shooterSize / 2 + Math.cos(shooterAimAngle) * aimArrowSize / 2);
            final int aimArrowY = (int)(shooterY - cameraY + shooterSize / 2 + Math.sin(shooterAimAngle) * aimArrowSize / 2);
            g2d.setColor(Color.yellow);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(shooterX - cameraX + shooterSize / 2, shooterY - cameraY + shooterSize / 2, aimArrowX, aimArrowY);

            //draw bullets
            try {
                for (Bullet bullet : bulletList) {
                    g2d.setColor(bullet.bulletColor);
                    g2d.fillOval((int) bullet.x - cameraX - bullet.bulletSize / 2, (int) bullet.y - cameraY - bullet.bulletSize / 2, bullet.bulletSize, bullet.bulletSize);
                }
            }
            catch (Exception e){
                System.out.println("bullet draw error");
            }

            //draw enemies
            AffineTransform originalAT = g2d.getTransform();
            //lv1
            try {
                for (EnemyLV1 enemy : enemyList) {
                    g2d.setColor(enemy.color);
                    //draw triangle
                    g2d.setTransform(originalAT);
                    int[] xPoints = {enemy.x - cameraX, enemy.x - cameraX + enemy.size / 2, enemy.x - cameraX - enemy.size / 2};
                    int[] yPoints = {enemy.y - cameraY - enemy.size / 2, enemy.y - cameraY + enemy.size / 2, enemy.y - cameraY + enemy.size / 2};
                    g2d.rotate(enemy.rotate, enemy.x - cameraX, enemy.y - cameraY);
                    g2d.fillPolygon(xPoints, yPoints, 3);
                }
            }
            catch (Exception e){
                System.out.println("enemy lv1 draw error");
            }
            repaint();
        }
    };
    MouseMotionAdapter shooterAimMouseMotion = new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            mouseCursorX = e.getX();
            mouseCursorY = e.getY();
            int shooterCenterX = shooterX - cameraX + shooterSize / 2;
            int shooterCenterY = shooterY - cameraY + shooterSize / 2;
            shooterAimAngle = Math.atan2(mouseCursorY - shooterCenterY, mouseCursorX - shooterCenterX);
        }
    };
    MouseAdapter shooterFireMouse = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            double bulletSpeed = 10;
            double bulletXVec = Math.cos(shooterAimAngle) * bulletSpeed;
            double bulletYVec = Math.sin(shooterAimAngle) * bulletSpeed;
            bulletList.add(new Bullet(shooterX + (double) shooterSize / 2, shooterY + (double) shooterSize / 2, bulletXVec, bulletYVec));
        }
    };

    ArrayList<Bullet> bulletList = new ArrayList<>();
    ArrayList<EnemyLV1> enemyList = new ArrayList<>();
    Thread bulletMoveThread = new Thread(() -> {
        while (true) {
            try {
                for (Bullet bullet : bulletList) {
                    bullet.x += bullet.xVec;
                    bullet.y += bullet.yVec;
                    //mainWindow collision
                    final double distance = Math.sqrt(Math.pow(bullet.x - shooterX, 2) + Math.pow(bullet.y - shooterY, 2));
                    int impulse = (int) (10000 / distance);
                    if (impulse > 10) {
                        impulse = 10;
                    }
                    if (bullet.x - (double) bullet.bulletSize / 2 < cameraX) {
                        windowNXSizeVec -= impulse;
                        bulletList.remove(bullet);
                        break;
                    }
                    if (bullet.x + (double) bullet.bulletSize / 2 > cameraX + cameraWidth) {
                        windowPXSizeVec += impulse;
                        bulletList.remove(bullet);
                        break;
                    }
                    if (bullet.y - (double) bullet.bulletSize / 2 < cameraY) {
                        windowNYSizeVec -= impulse;
                        bulletList.remove(bullet);
                        break;
                    }
                    if (bullet.y + (double) bullet.bulletSize / 2 > cameraY + cameraHeight) {
                        windowPYSizeVec += impulse;
                        bulletList.remove(bullet);
                        break;
                    }
                }
            }
            catch (Exception e){
                System.out.println("bullet move error");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
    Thread enemyGeneratorThread = new Thread(() -> {
        Random random = new Random();
        while (true) {
            if(random.nextInt(0,100) < 50) {
                int enemyX = (int) (Math.random() * screenMaxWidth);
                int enemyY = (int) (Math.random() * screenMaxHeight);
                int enemyXVec = (int) (random.nextDouble(-3, 3));
                int enemyYVec = (int) (random.nextDouble(-3, 3));
                enemyList.add(new EnemyLV1(enemyX, enemyY, enemyXVec, enemyYVec));
                System.out.println("enemy generated");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
    final int minimumCameraXSize = 500;
    final int minimumCameraYSize = 300;
    Thread cameraAdjustThread = new Thread(() -> {
        while (true) {
            setLocation(cameraX, cameraY);
            setSize(cameraWidth, cameraHeight);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }

            cameraX += windowNXSizeVec;
            cameraY += windowNYSizeVec;
            final int cameraWidthCalculate = windowPXSizeVec - windowNXSizeVec;
            final int cameraHeightCalculate = windowPYSizeVec - windowNYSizeVec;
            cameraWidth += cameraWidthCalculate;
            cameraHeight += cameraHeightCalculate;

            if(cameraX < 0){
                cameraX = 0;
                if(windowNXSizeVec < 0){
                    windowNXSizeVec *= -1;
                }
            }
            else if(cameraX > screenMaxWidth - cameraWidth){
                cameraX = screenMaxWidth - cameraWidth;
                if(windowPXSizeVec > 0){
                    windowPXSizeVec *= -1;
                }
            }
            if(cameraY < 0){
                cameraY = 0;
                if(windowNYSizeVec < 0){
                    windowNYSizeVec *=-1;
                }
            }
            else if(cameraY > screenMaxHeight - cameraHeight){
                cameraY = screenMaxHeight - cameraHeight;
                if(windowPYSizeVec > 0){
                    windowPYSizeVec *=-1;
                }
            }

            final int shooterXCenter = shooterX - cameraX + shooterSize / 2;
            final int shooterYCenter = shooterY - cameraY + shooterSize / 2;

            final int pXShrinkLimit = (int) ((cameraWidth - shooterXCenter) / (cameraWidth /2.0) * -4);
            final int nXShrinkLimit = (int) (shooterXCenter /(cameraWidth/2.0) * 4);
            final int pYShrinkLimit = (int) ((cameraHeight - shooterYCenter) / (cameraHeight /2.0) * -4);
            final int nYShrinkLimit = (int) (shooterYCenter /(cameraHeight/2.0) * 4);

            windowNXSizeVec++;
            if(windowNXSizeVec > nXShrinkLimit){
                windowNXSizeVec = nXShrinkLimit;
            }
            windowPXSizeVec--;
            if(windowPXSizeVec < pXShrinkLimit){
                windowPXSizeVec = pXShrinkLimit;
            }
            windowNYSizeVec++;
            if(windowNYSizeVec > nYShrinkLimit){
                windowNYSizeVec = nYShrinkLimit;
            }
            windowPYSizeVec--;
            if(windowPYSizeVec < pYShrinkLimit){
                windowPYSizeVec = pYShrinkLimit;
            }

            if(cameraWidth < minimumCameraXSize){
                if(windowNXSizeVec > 0){
                    windowNXSizeVec = 0;
                }
                if(windowPXSizeVec < 0){
                    windowPXSizeVec = 0;
                }
            }
            if(cameraHeight < minimumCameraYSize) {
                if(windowNYSizeVec > 0){
                    windowNYSizeVec = 0;
                }
                if(windowPYSizeVec < 0){
                    windowPYSizeVec = 0;
                }
            }

            if(shooterXCenter < 50){
                if(windowNXSizeVec > 0) {
                    windowNXSizeVec = 0;
                }
                else{
                    windowNXSizeVec -=1;
                }
                if(cameraX <50){
                    cameraX = 0;
                }
                else{
                    cameraX -= 50-shooterXCenter;
                    cameraWidth += 50-shooterXCenter;
                }
            }
            if(shooterXCenter > cameraWidth - 50){
                if(windowPXSizeVec < 0) {
                    windowPXSizeVec = 0;
                }
                else{
                    windowPXSizeVec +=1;
                }
                if(cameraX + cameraWidth > screenMaxWidth-50){
                    cameraWidth = screenMaxWidth - cameraX;
                }
                else {
                    cameraWidth += 50 - (cameraWidth - shooterXCenter);
                }
            }
            if(shooterYCenter < 50){
                if(windowNYSizeVec > 0) {
                    windowNYSizeVec = 0;
                }
                else{
                    windowNYSizeVec -=1;
                }
                if(cameraY <50){
                    cameraY = 0;
                }
                else{
                    cameraY -= 50-shooterYCenter;
                    cameraHeight += 50-shooterYCenter;
                }
            }
            if(shooterYCenter > cameraHeight - 50){
                if(windowPYSizeVec < 0) {
                    windowPYSizeVec = 0;
                }
                else{
                    windowPYSizeVec +=1;
                }
                if(cameraY + cameraHeight > screenMaxHeight-50){
                    cameraHeight = screenMaxHeight - cameraY;
                }
                else{
                    cameraHeight += 50 - (cameraHeight - shooterYCenter);
                }
            }


        }
    });
    Thread enemyBulletCollisionThread = new Thread(() -> {
        while (true) {
            try {
                for (Bullet bullet : bulletList) {
                    for (EnemyLV1 enemy : enemyList) {
                        final double distance = Math.sqrt(Math.pow(bullet.x - enemy.x, 2) + Math.pow(bullet.y - enemy.y, 2));
                        if (distance < enemy.size / 2) {
                            enemy.dispose();
                            bulletList.remove(bullet);
                            enemyList.remove(enemy);
                            break;
                        }
                    }
                }
            }
            catch (Exception e){
                System.out.println("enemy bullet collision error");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
    KeyAdapter shooterMoveKey = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case 'w':
                case 'W':
                    shooterYVec = -10;
                    break;
                case KeyEvent.VK_DOWN:
                case 's':
                case 'S':
                    shooterYVec = 10;
                    break;
                case KeyEvent.VK_LEFT:
                case 'a':
                case 'A':
                    shooterXVec = -10;
                    break;
                case KeyEvent.VK_RIGHT:
                case 'd':
                case 'D':
                    shooterXVec = 10;
                    break;
            }
        }
        public void keyReleased(KeyEvent e){
            shooterXVec=0;
            shooterYVec=0;
        }
    };
    Thread shooterMoveThread = new Thread(() -> {
        while (true) {
            shooterX += shooterXVec;
            shooterY += shooterYVec;

            if(shooterX<0){
                shooterX=0;
            }
            else if(shooterX>screenMaxWidth-shooterSize){
                shooterX=screenMaxWidth-shooterSize;
            }
            if(shooterY<0){
                shooterY=0;
            }
            else if(shooterY>screenMaxHeight-shooterSize){
                shooterY=screenMaxHeight-shooterSize;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }
    });

    public Shooter() {
        try{
            screenMaxHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            screenMaxWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        }
        catch (Exception e){
            System.out.println("screen size error");
            screenMaxHeight = 1080;
            screenMaxWidth = 1920;
        }
        setSize(cameraWidth, cameraHeight);
        setUndecorated(true);
        setLocationRelativeTo(null);
        cameraX = getLocation().x;
        cameraY = getLocation().y;
        add(shooterScreenPanel);
        shooterScreenPanel.setBackground(Color.black);
        shooterScreenPanel.addMouseMotionListener(shooterAimMouseMotion);
        shooterScreenPanel.addMouseListener(shooterFireMouse);
        addMouseMotionListener(shooterAimMouseMotion);
        addMouseListener(shooterFireMouse);
        addKeyListener(shooterMoveKey);
        setVisible(true);
        bulletMoveThread.start();
        cameraAdjustThread.start();
        shooterMoveThread.start();
        enemyGeneratorThread.start();
        gravityEffectThread.start();
        enemyBulletCollisionThread.start();
        requestFocusThread.start();
    }
    public static void main(String[] args) {
        new Shooter();
    }
    Thread gravityEffectThread = new Thread(() -> {
        while (true) {
            try {
                for(EnemyLV1 enemy : enemyList){
                    int xDiff = shooterX - enemy.x;
                    int yDiff = shooterY - enemy.y;

                    enemy.xVec += xDiff / 1000;
                    enemy.yVec += yDiff / 1000;
                }
            }
            catch (Exception e){
                System.out.println("gravity effect error");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
}

class Bullet {
    double x;
    double y;
    double xVec;
    double yVec;
    int bulletSize = 10;
    Color bulletColor = Color.red;

    public Bullet(double x, double y, double xVec, double yVec) {
        this.x = x;
        this.y = y;
        this.xVec = xVec;
        this.yVec = yVec;
    }
}
class EnemyLV1 extends JFrame {
    int x;
    int y;
    double rotate=0;
    final int size = 50;
    int xVec;
    int yVec;
    final int speedLimit = 5;
    final private int screenMaxWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    final private int screenMaxHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    double rotateSpeed;
    Color color = Color.red;
    Thread moveThread = new Thread(() -> {
        while (true) {
            x += xVec;
            y += yVec;
            rotate += rotateSpeed;
            if(x<0){
                xVec *= -1;
            }
            else if(x>screenMaxWidth-size){
                xVec *= -1;
            }
            if(y<0){
                yVec *= -1;
            }
            else if(y>screenMaxHeight-size){
                yVec *= -1;
            }

            int currentSpeed = (int) Math.sqrt(Math.pow(xVec,2) + Math.pow(yVec,2));
            if(currentSpeed > speedLimit){
                xVec = (int) (xVec / currentSpeed * speedLimit);
                yVec = (int) (yVec / currentSpeed * speedLimit);
            }


            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
    Thread windowMoveThread = new Thread(() -> {
        while (true) {
            setLocation(x-size/2, y-size/2);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }
    });
    JPanel enemyPanel = new JPanel() {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(color);
            int[] xPoints = {x-size/2, x, x+size/2};
            int[] yPoints = {y+size/2, y-size/2, y+size/2};
            g2d.rotate(rotate, x, y);
            g2d.fillPolygon(xPoints, yPoints, 3);
            repaint();
        }
    };
    public EnemyLV1(int x, int y, int xVec, int yVec) {
        this.x = x;
        this.y = y;
        this.xVec = xVec;
        this.yVec = yVec;
        rotateSpeed = 0.1;
        setSize(100, 100);
        setLocation(x-size/2, y-size/2);
        setUndecorated(true);
        add(enemyPanel);
        enemyPanel.setBackground(Color.black);
        setVisible(true);
        moveThread.start();
        windowMoveThread.start();
    }
}