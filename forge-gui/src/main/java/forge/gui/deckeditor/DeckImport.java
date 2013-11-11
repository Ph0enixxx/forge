/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gui.deckeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;

import org.apache.commons.lang3.StringUtils;

import net.miginfocom.swing.MigLayout;
import forge.deck.Deck;
import forge.deck.DeckBase;
import forge.deck.DeckRecognizer;
import forge.deck.DeckRecognizer.TokenType;
import forge.deck.DeckSection;
import forge.gui.deckeditor.controllers.ACEditorBase;
import forge.gui.toolbox.FButton;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FComboBoxWrapper;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FTextArea;
import forge.item.PaperCard;
import forge.item.InventoryItem;
import forge.view.FDialog;

/**
 * 
 * Dialog for quick import of decks.
 *
 * @param <TItem>
 * @param <TModel>
 */
public class DeckImport<TItem extends InventoryItem, TModel extends DeckBase> extends FDialog {
    private static final long serialVersionUID = -5837776824284093004L;

    private final FTextArea txtInput = new FTextArea();
    private static final String STYLESHEET = "<style>"
            + "body, h1, h2, h3, h4, h5, h6, table, tr, td, p {margin: 3px 1px; padding: 0; font-weight: "
            + "normal; font-style: normal; text-decoration: none; font-family: Arial; font-size: 10px;} "
            +
            // "h1 {border-bottom: solid 1px black; color: blue; font-size: 12px; margin: 3px 0 9px 0; } "
            // +
            ".comment {color: #666666;} " + ".knowncard {color: #009900;} " + ".unknowncard {color: #990000;} "
            + ".section {padding: 3px 10px; margin: 3px 0; font-weight: 700; background-color: #DDDDDD; } "
            + "</style>";
    private static final String HTML_WELCOME_TEXT = "<html>"
            + DeckImport.STYLESHEET
            + "<h3>You'll see recognized cards here</h3>"
            + "<div class='section'>Legend</div>"
            + "<ul>"
            + "<li class='knowncard'>Recognized cards will be shown in green. These cards will be auto-imported into a new deck<BR></li>"
            + "<li class='unknowncard'>Lines which seem to be cards but are either misspelled or unsupported by Forge, are shown in dark-red<BR></li>"
            + "<li class='comment'>Lines that appear unsignificant will be shown in gray<BR><BR></li>" + "</ul>"
            + "<div class='section'>Choosing source</div>"
            + "<p>In most cases when you paste from clipboard a carefully selected area of a webpage, it works perfectly.</p>"
            + "<p>Sometimes to filter out unneeded data you may have to export deck in MTGO format, and paste here downloaded file contents.</p>"
            + "<p>Sideboard recognition is supported. Make sure that the sideboard cards are listed after a line that contains the word 'Sideboard'</p>"
            + "</html>";

    private final JEditorPane htmlOutput = new JEditorPane("text/html", DeckImport.HTML_WELCOME_TEXT);
    private final FScrollPane scrollInput = new FScrollPane(this.txtInput);
    private final FScrollPane scrollOutput = new FScrollPane(this.htmlOutput);
    private final FLabel summaryMain = new FLabel.Builder().text("Imported deck summary will appear here").build();
    private final FLabel summarySide = new FLabel.Builder().text("Line for sideboard summary").build();
    private final FButton cmdAccept = new FButton("Import Deck");
    private final FButton cmdCancel = new FButton("Cancel");
    private final FCheckBox newEditionCheck = new FCheckBox("Import latest version of card", true);
    private final FCheckBox dateTimeCheck = new FCheckBox("Use only sets released before:", false);
    
    private final FComboBoxWrapper<String> monthDropdown = new FComboBoxWrapper<>();
    private final FComboBoxWrapper<Integer> yearDropdown = new FComboBoxWrapper<>();

    /** The tokens. */
    private final List<DeckRecognizer.Token> tokens = new ArrayList<DeckRecognizer.Token>();

    private final ACEditorBase<TItem, TModel> host;

    /**
     * Instantiates a new deck import.
     * 
     * @param g
     *            the g
     */
    public DeckImport(final ACEditorBase<TItem, TModel> g) {
        this.host = g;

        final int wWidth = 600;
        final int wHeight = 600;

        this.setPreferredSize(new java.awt.Dimension(wWidth, wHeight));
        this.setSize(wWidth, wHeight);
        this.setResizable(false);
        this.setTitle("Deck Importer");
        
        FPanel fp = new FPanel(new MigLayout("fill"));
        this.add(fp);

        txtInput.setFocusable(true);
        txtInput.setEditable(true);
        
        this.htmlOutput.setEditable(false);

        this.scrollInput.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "Paste or type a decklist"));
        this.scrollOutput.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
                "Expect the recognized lines to appear"));
        this.scrollInput.setViewportBorder(BorderFactory.createLoweredBevelBorder());
        this.scrollOutput.setViewportBorder(BorderFactory.createLoweredBevelBorder());

        
        fp.add(this.scrollInput, "cell 0 0, w 50%, growy, pushy");
        fp.add(this.newEditionCheck, "cell 0 1, w 50%, ax c");
        fp.add(this.dateTimeCheck, "cell 0 2, w 50%, ax c");
        
        monthDropdown.addTo(fp, "cell 0 3, w 20%, ax r, split 2");
        yearDropdown.addTo(fp, "w 15%, pad 0 0 0 -10");
        fillDateDropdowns();
        
        fp.add(this.scrollOutput, "cell 1 0, w 50%, growy, pushy");
        fp.add(this.summaryMain, "cell 1 1, label");
        fp.add(this.summarySide, "cell 1 2, label");

        fp.add(this.cmdAccept, "cell 1 3, split 2, w 140, align c, h 26, pad 0 0 0 -20");
        fp.add(this.cmdCancel, "w 120, h 26");


        this.cmdCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DeckImport.this.processWindowEvent(new WindowEvent(DeckImport.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        this.cmdAccept.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String warning = "This will replace contents of your currently open deck with whatever you are importing. Proceed?";
                final int answer = JOptionPane.showConfirmDialog(DeckImport.this, warning, "Replacing old deck",
                        JOptionPane.YES_NO_OPTION);
                if (JOptionPane.NO_OPTION == answer) {
                    return;
                }
                final Deck toSet = DeckImport.this.buildDeck();
                DeckImport.this.host.getDeckController().setModel((TModel) toSet);
                DeckImport.this.processWindowEvent(new WindowEvent(DeckImport.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        ActionListener updateDateCheck = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSel = dateTimeCheck.isSelected();
                monthDropdown.setEnabled(isSel);
                yearDropdown.setEnabled(isSel);
                parseAndDisplay();
            }
        };
        this.dateTimeCheck.addActionListener(updateDateCheck);
        
        ActionListener reparse = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { parseAndDisplay(); }
        };
        this.newEditionCheck.addActionListener(reparse);
        this.yearDropdown.addActionListener(reparse);
        this.monthDropdown.addActionListener(reparse);
        updateDateCheck.actionPerformed(null); // update actual state

        this.txtInput.getDocument().addDocumentListener(new OnChangeTextUpdate());
        this.cmdAccept.setEnabled(false);
    }

    /**
     * TODO: Write javadoc for this method.
     */
    private void fillDateDropdowns() {
        DateFormatSymbols dfs = new DateFormatSymbols();
        monthDropdown.removeAllItems();
        String[] months = dfs.getMonths();
        for(String monthName : months)
            if(!StringUtils.isBlank(monthName))
                monthDropdown.addItem(monthName);
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = yearNow; i >= 1993; i--)
            yearDropdown.addItem(Integer.valueOf(i));
    }

    private void readInput() {
        this.tokens.clear();
        final ElementIterator it = new ElementIterator(this.txtInput.getDocument().getDefaultRootElement());
        Element e;
        
        DeckRecognizer recognizer = new DeckRecognizer(newEditionCheck.isSelected());
        if(dateTimeCheck.isSelected())
            recognizer.setDateConstraint(monthDropdown.getSelectedIndex(), yearDropdown.getSelectedItem());
        
        while ((e = it.next()) != null) {
            if (!e.isLeaf()) {
                continue;
            }
            final int rangeStart = e.getStartOffset();
            final int rangeEnd = e.getEndOffset();
            try {
                final String line = this.txtInput.getText(rangeStart, rangeEnd - rangeStart);
                this.tokens.add(recognizer.recognizeLine(line));
            } catch (final BadLocationException ex) {
            }
        }
    }

    private void displayTokens() {
        if(this.tokens.isEmpty())
            this.htmlOutput.setText(HTML_WELCOME_TEXT);
        else {
            final StringBuilder sbOut = new StringBuilder("<html>");
            sbOut.append(DeckImport.STYLESHEET);
            for (final DeckRecognizer.Token t : this.tokens) {
                sbOut.append(this.makeHtmlViewOfToken(t));
            }
            sbOut.append("</html>");
            this.htmlOutput.setText(sbOut.toString());
        }
    }

    private void updateSummaries() {
        final int[] cardsOk = new int[2];
        final int[] cardsUnknown = new int[2];
        int idx = 0;
        for (final DeckRecognizer.Token t : this.tokens) {
            if (t.getType() == TokenType.KnownCard) {
                cardsOk[idx] += t.getNumber();
            }
            if (t.getType() == TokenType.UnknownCard) {
                cardsUnknown[idx] += t.getNumber();
            }
            if ((t.getType() == TokenType.SectionName) && t.getText().toLowerCase().contains("side")) {
                idx = 1;
            }
        }
        this.summaryMain.setText(String.format("Main: %d cards recognized, %d unknown cards", cardsOk[0],
                cardsUnknown[0]));
        this.summarySide.setText(String.format("Sideboard: %d cards recognized, %d unknown cards", cardsOk[1],
                cardsUnknown[1]));
        this.cmdAccept.setEnabled(cardsOk[0] > 0);
    }

    private Deck buildDeck() {
        final Deck result = new Deck();
        boolean isMain = true;
        for (final DeckRecognizer.Token t : this.tokens) {
            final DeckRecognizer.TokenType type = t.getType();
            if ((type == DeckRecognizer.TokenType.SectionName) && t.getText().toLowerCase().contains("side")) {
                isMain = false;
            }
            if (type != DeckRecognizer.TokenType.KnownCard) {
                continue;
            }
            final PaperCard crd = t.getCard();
            if (isMain) {
                result.getMain().add(crd, t.getNumber());
            } else {
                result.getOrCreate(DeckSection.Sideboard).add(crd, t.getNumber());
            }
        }
        return result;
    }

    protected void parseAndDisplay() {
        readInput();
        displayTokens();
        updateSummaries();
    }
    
    /**
     * The Class OnChangeTextUpdate.
     */
    protected class OnChangeTextUpdate implements DocumentListener {
        private void onChange() {
            DeckImport.this.parseAndDisplay();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#insertUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public final void insertUpdate(final DocumentEvent e) {
            this.onChange();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#removeUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public final void removeUpdate(final DocumentEvent e) {
            this.onChange();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#changedUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void changedUpdate(final DocumentEvent e) {
        } // Happend only on ENTER pressed
    }

    private String makeHtmlViewOfToken(final DeckRecognizer.Token token) {
        switch (token.getType()) {
        case KnownCard:
            return String.format("<div class='knowncard'>%s * %s [%s] %s</div>", token.getNumber(), token.getCard()
                    .getName(), token.getCard().getEdition(), token.getCard().isFoil() ? "<i>foil</i>" : "");
        case UnknownCard:
            return String.format("<div class='unknowncard'>%s * %s</div>", token.getNumber(), token.getText());
        case SectionName:
            return String.format("<div class='section'>%s</div>", token.getText());
        case UnknownText:
        case Comment:
            return String.format("<div class='comment'>%s</div>", token.getText());
        default:
            break;
        }
        return "";
    }

}