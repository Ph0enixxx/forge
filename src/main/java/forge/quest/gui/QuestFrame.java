package forge.quest.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import forge.AllZone;
import forge.gui.GuiUtils;
import forge.quest.gui.bazaar.QuestBazaarPanel;
import forge.view.swing.Gui_HomeScreen;
import forge.view.swing.OldGuiNewGame;

/**
 * <p>
 * QuestFrame class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestFrame extends JFrame {
    /** Constant <code>serialVersionUID=-2832625381531838412L</code>. */
    private static final long serialVersionUID = -2832625381531838412L;

    /** The visible panel. */
    JPanel visiblePanel;

    /** The quest layout. */
    CardLayout questLayout;

    /** Constant <code>MAIN_PANEL="Main"</code>. */
    public static final String MAIN_PANEL = "Main";

    /** Constant <code>BAZAAR_PANEL="Bazaar"</code>. */
    public static final String BAZAAR_PANEL = "Bazaar";

    /** The sub panel map. */
    Map<String, QuestAbstractPanel> subPanelMap = new HashMap<String, QuestAbstractPanel>();

    /**
     * <p>
     * Constructor for QuestFrame.
     * </p>
     * 
     * @throws HeadlessException
     *             the headless exception
     */
    public QuestFrame() throws HeadlessException {
        this.setTitle("Quest Mode");

        visiblePanel = new JPanel(new BorderLayout());
        visiblePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        questLayout = new CardLayout();
        visiblePanel.setLayout(questLayout);

        QuestAbstractPanel newPanel = new QuestMainPanel(this);
        visiblePanel.add(newPanel, MAIN_PANEL);
        subPanelMap.put(MAIN_PANEL, newPanel);

        newPanel = new QuestBazaarPanel(this);
        visiblePanel.add(newPanel, BAZAAR_PANEL);
        subPanelMap.put(BAZAAR_PANEL, newPanel);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(visiblePanel, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(1024, 768));
        this.setMinimumSize(new Dimension(800, 600));

        questLayout.show(visiblePanel, MAIN_PANEL);

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        GuiUtils.centerFrame(this);

    }

    /**
     * <p>
     * showPane.
     * </p>
     * 
     * @param paneName
     *            a {@link java.lang.String} object.
     */
    private void showPane(final String paneName) {
        subPanelMap.get(paneName).refreshState();
        questLayout.show(visiblePanel, paneName);
    }

    /**
     * <p>
     * showMainPane.
     * </p>
     */
    public final void showMainPane() {
        showPane(MAIN_PANEL);
    }

    /**
     * <p>
     * showBazaarPane.
     * </p>
     */
    public final void showBazaarPane() {
        showPane(BAZAAR_PANEL);
    }

    /**
     * <p>
     * returnToMainMenu.
     * </p>
     */
    public final void returnToMainMenu() {
        AllZone.getQuestData().saveData();

        if (System.getenv("NG2") != null) {
            if (System.getenv("NG2").equalsIgnoreCase("true")) {
                String[] argz = {};
                Gui_HomeScreen.main(argz);
            } else {
                new OldGuiNewGame();
            }
        } else {
            new OldGuiNewGame();
        }

        this.dispose();
    }
}
