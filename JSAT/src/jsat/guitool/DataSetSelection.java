
package jsat.guitool;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jsat.DataSet;

/**
 *
 * @author Edward Raff
 */
public class DataSetSelection extends JDialog
{


	private static final long serialVersionUID = -2599749949210575182L;
	final String[] dataSelections;
    final String[] reasons;
    JComboBox[] boxs;

    public DataSetSelection(Frame parent, String title, DataSet dataSet, String[] reasons)
    {
        super(parent, title, true);
        this.dataSelections = new String[dataSet.getNumNumericalVars()];
        for(int i = 0; i < dataSelections.length; i++)
            this.dataSelections[i] = dataSet.getNumericName(i);
        this.reasons = reasons;
        boxs = new JComboBox[reasons.length];
        
        JPanel optionPanel = new JPanel(new GridLayout(reasons.length, 1));
        JPanel fullPanel = new JPanel(new BorderLayout());
        


        for(int i = 0; i < reasons.length; i++)
        {
            final JComboBox jc = new JComboBox(dataSelections);
            jc.setBorder(BorderFactory.createTitledBorder(reasons[i]));

            boxs[i] = jc;
            optionPanel.add(jc);
        }

        fullPanel.add(new JScrollPane(optionPanel), BorderLayout.CENTER);


        JButton closeButton = new JButton("Ok");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                setVisible(false); 
            }
        });

        fullPanel.add(closeButton, BorderLayout.SOUTH);

        setContentPane(fullPanel);

    }

    public int[] getSelections()
    {
        int[] selections = new int[reasons.length];
        pack();
        setVisible(true);
        for(int i = 0; i < reasons.length; i++)
            selections[i] = boxs[i].getSelectedIndex();

        return selections;
    }


}
