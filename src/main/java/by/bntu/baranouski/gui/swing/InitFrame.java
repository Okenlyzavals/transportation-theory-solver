package by.bntu.baranouski.gui.swing;

import com.formdev.flatlaf.FlatLightLaf;
import org.softsmithy.lib.swing.JIntegerField;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Optional;

public class InitFrame {
    private static JPanel dialogPanel;
    private static JIntegerField producerNumber;
    private static JIntegerField consumerNumber;
    private static JFrame dummy;

    static {
        try {
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e){System.exit(1);}
        producerNumber = new IntegerFieldEmptyOnFocus(1,999);
        producerNumber.setEditable(true);
        producerNumber.setHorizontalAlignment(SwingConstants.CENTER);
        producerNumber.setColumns(10);
        consumerNumber = new IntegerFieldEmptyOnFocus(1,999);
        consumerNumber.setHorizontalAlignment(SwingConstants.CENTER);
        consumerNumber.setEditable(true);
        consumerNumber.setColumns(10);

        dialogPanel = new JPanel();
        dialogPanel.add(new JLabel("Producers: "));
        dialogPanel.add(producerNumber);
        dialogPanel.add(new JLabel("Consumers: "));
        dialogPanel.add(consumerNumber);
    }

    public static void main(String[] args) {
        dialog().ifPresent(params->
                new SplashScreen(params[0], params[1]));
    }

    public static Optional<int[]> dialog(){
        int[] params = new int[2];

        if (dummy == null){
            dummy = new JFrame();
        }
        dummy.setLocationRelativeTo(null);
        dummy.setAlwaysOnTop(true);
        int result = JOptionPane.showConfirmDialog(dummy, dialogPanel,
                "Set grid size", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            params[0] = producerNumber.getIntValue();
            params[1] = consumerNumber.getIntValue();
            return Optional.of(params);
        }
        dummy.dispose();
        return Optional.empty();
    }

    private static class IntegerFieldEmptyOnFocus extends JIntegerField{
        private IntegerFieldEmptyOnFocus(int min, int max){
            super(min, max);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    super.focusGained(e);
                    if (getIntValue() == getMinimumIntValue()) {
                        setText("");
                    }
                }
            });
        }
    }
}
