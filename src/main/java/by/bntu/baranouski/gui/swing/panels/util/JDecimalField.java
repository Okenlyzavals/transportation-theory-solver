package by.bntu.baranouski.gui.swing.panels.util;

import org.softsmithy.lib.swing.JRealNumberField;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;

public class JDecimalField extends JRealNumberField {
    public JDecimalField(){
        super();
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getBigDecimalValue().equals(getMinimumBigDecimalValue())
                        || getBigDecimalValue().equals(BigDecimal.ZERO)) {
                    JDecimalField.this.setText("");
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
