package by.bntu.baranouski.gui.swing.panels.util;

import org.softsmithy.lib.swing.JRealNumberField;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;

public class JDecimalField extends JRealNumberField {
    public JDecimalField(){
        super();
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (getBigDecimalValue().equals(getMinimumBigDecimalValue())) {
                    setText("");
                }
            }
        });
    }

    @Override
    public BigDecimal getBigDecimalValue() {
        try {
            return new BigDecimal(getText().replace(" ","").replace(',','.'));
        } catch (NumberFormatException e){
            return super.getBigDecimalValue();}
    }
}
