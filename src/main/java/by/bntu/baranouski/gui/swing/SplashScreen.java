package by.bntu.baranouski.gui.swing;

import by.bntu.baranouski.gui.swing.panels.util.GridBagHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class SplashScreen extends JFrame {

    private final JLabel loadingLabel = new JLabel("",SwingConstants.CENTER);
    private final JPanel uiPanel = new JPanel(new GridBagLayout());
    private MainFrame mainFrame;

    public SplashScreen(int producerNum, int consumerNum){
        createUI();
        setVisible(true);
        init(producerNum, consumerNum);
    }

    private void init(int producerNum, int consumerNum){
        ActionListener loadingListener = new LoadingListener();
        Timer loadingTimer = new Timer(500, loadingListener);
        loadingTimer.start();

        mainFrame  = new MainFrame(producerNum, consumerNum);
        loadingLabel.setText("ANY KEY to continue | ESC to exit");
        repaint();
        loadingTimer.removeActionListener(loadingListener);
        loadingTimer.addActionListener(new BlinkingTimerListener(Color.RED));
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    System.exit(0);
                } else {
                    setVisible(false);
                    mainFrame.setVisible(true);
                    SplashScreen.this.dispose();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                mainFrame.setVisible(true);
                SplashScreen.this.dispose();
            }
        });
    }

    private void createUI(){

        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600,380);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon(Objects
                .requireNonNull(getClass().getClassLoader().getResource("icon.png")));
        setIconImage(icon.getImage());

        GridBagHelper helper = new GridBagHelper();

        helper.nextCell().span();
        uiPanel.add(new JLabel("Belarusian National Technical University"), helper.get());
        helper.nextRow().nextCell().span();
        uiPanel.add(new JLabel("Faculty of Information Technology and Robotics"), helper.get());
        helper.nextRow().nextCell().span().setInsets(0,0,0,30);
        uiPanel.add(new JLabel("Department of software of information systems and technologies"), helper.get());

        JLabel courseWork = new JLabel("Course project");
        courseWork.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        helper.nextRow().nextCell().span();
        uiPanel.add(courseWork, helper.get());

        helper.nextRow().nextCell().span();
        uiPanel.add(new JLabel("For discipline \"Project Solutions' Optimization\""), helper.get());

        JLabel topic = new JLabel("Transportation Theory");
        topic.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        helper.nextRow().nextCell().span();
        uiPanel.add(topic, helper.get());

        ImageIcon demoIcon = new ImageIcon(Objects
                .requireNonNull(
                        getClass().getClassLoader()
                        .getResource("icon.png")));
        demoIcon.setImage(demoIcon.getImage().getScaledInstance(100,100,Image.SCALE_SMOOTH));


        JPanel studentDataWrapper = new JPanel(new GridLayout(1,2));

        studentDataWrapper.add(new JLabel(demoIcon));

        JPanel studentDataPanel = new JPanel();
        studentDataPanel.setLayout(new BoxLayout(studentDataPanel, BoxLayout.Y_AXIS));
        studentDataPanel.add(new JLabel("Author: group 10702419 student"));
        studentDataPanel.add(new JLabel("Baranouski Yauheni Kirylavich"));
        studentDataPanel.add(new JLabel("\n"));
        studentDataPanel.add(new JLabel("Instructor: c.t.s., docent"));
        studentDataPanel.add(new JLabel("Kovaleva Irina Lvovna"));
        studentDataWrapper.add(studentDataPanel);

        helper.nextRow().nextCell().setInsets(0,5,0,10);
        uiPanel.add(studentDataWrapper, helper.get());
        helper.nextCell();

        helper.nextRow().nextCell().setInsets(0,5,0,15).span();
        uiPanel.add(new JLabel("Minsk, 2022"), helper.get());

        helper.nextRow().nextCell().span();
        uiPanel.add(loadingLabel, helper.get());

        uiPanel.setBorder(new EmptyBorder(10,10,10,10));
        getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add(uiPanel);

    }

    private class BlinkingTimerListener implements ActionListener{
        private final Color blinkColor;
        private final Color noBlinkColor;
        private boolean isForeground = true;

        private BlinkingTimerListener(Color blinkColor){
            noBlinkColor = loadingLabel.getForeground();
            this.blinkColor = blinkColor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isForeground) {
                loadingLabel.setForeground(noBlinkColor);
            }
            else {
                loadingLabel.setForeground(blinkColor);
            }
            isForeground = !isForeground;
        }
    }

    private class LoadingListener implements ActionListener{

        private final String[] values = {"Loading.", "Loading..", "Loading..."};
        private int iterator = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            loadingLabel.setText(values[iterator]);
            iterator = (iterator == values.length-1) ? 0 : iterator+1;
        }
    }
}
