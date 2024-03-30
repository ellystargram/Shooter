import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class WindowDebug extends JFrame {
    double windowXSize = 300;
    double windowYSize = 200;
    double windowXLocation = 100;
    double windowYLocation = 100;
    Thread windowLocationUpdateThread = new Thread(() -> {
        while (true) {
            setLocation((int)windowXLocation, (int)windowYLocation);
            setSize((int)windowXSize, (int)windowYSize);
        }
    });
    KeyAdapter windowMoveKeyEvent = new KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent e) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case 'w':
                case 'W':
                    windowYLocation -= 10;
                    break;
                case KeyEvent.VK_DOWN:
                case 's':
                case 'S':
                    windowYLocation += 10;
                    break;
                case KeyEvent.VK_LEFT:
                case 'a':
                case 'A':
                    windowXLocation -= 10;
                    break;
                case KeyEvent.VK_RIGHT:
                case 'd':
                case 'D':
                    windowXLocation += 10;
                    break;
            }
        }
    };
    KeyAdapter windowSizeKeyEvent = new KeyAdapter(){
        public void keyPressed(java.awt.event.KeyEvent e) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case 'i':
                case 'I':
                    windowYSize -= 10;
                    break;
                case 'k':
                case 'K':
                    windowYSize += 10;
                    break;
                case 'j':
                case 'J':
                    windowXSize -= 10;
                    break;
                case 'l':
                case 'L':
                    windowXSize += 10;
                    break;
            }
        }
    };
    public static void main(String[] args) {
        new WindowDebug();
    }
    WindowDebug(){
        setTitle("Debugging");
        setSize((int)windowXSize, (int)windowYSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setVisible(true);
        addKeyListener(windowMoveKeyEvent);
        addKeyListener(windowSizeKeyEvent);
        windowLocationUpdateThread.start();
    }
}
