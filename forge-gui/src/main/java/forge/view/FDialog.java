package forge.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import forge.gui.SOverlayUtils;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FSkin;

@SuppressWarnings("serial")
public class FDialog extends JDialog implements ITitleBarOwner {
    private static final FSkin.SkinColor borderColor = FSkin.getColor(FSkin.Colors.CLR_BORDERS);
    private static final int cornerDiameter = 20;
    
    private FSkin.WindowSkin<FDialog> skin = FSkin.get(this);
    private Point locBeforeMove;
    private Point mouseDownLoc;
    private final FTitleBar titleBar;
    private final FPanel innerPanel;

    public FDialog() {
        this(true);
    }

    public FDialog(boolean modal0) {
        super(JOptionPane.getRootFrame(), modal0);
        this.setUndecorated(true);
        skin.setIconImage(FSkin.getIcon(FSkin.InterfaceIcons.ICO_FAVICON)); //use Forge icon by default

        this.innerPanel = new FPanel(new MigLayout("insets dialog, gap 0, center, fill"));
        this.innerPanel.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));
        this.innerPanel.setBackgroundTextureOverlay(FSkin.getColor(FSkin.Colors.CLR_THEME)); //use theme color as overlay to reduce background texture opacity
        this.innerPanel.setBorderToggle(false);
        this.innerPanel.setOpaque(false);
        super.setContentPane(this.innerPanel);

        this.titleBar = new FTitleBar(this);
        this.titleBar.setVisible(true);
        addMoveSupport();
    }

    @Override
    public void paint(Graphics g) {
        //set rounded rectangle shape for dialog
        int width = this.getWidth();
        int height = this.getHeight();
        int arc = cornerDiameter - 4; //leave room for border aliasing
        this.setShape(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));

        super.paint(g);

        //draw rounded border
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        skin.setGraphicsColor(g2d, borderColor);
        g2d.drawRoundRect(0, 0, width - 1, height - 1, cornerDiameter, cornerDiameter);
        g2d.dispose();
    }

    @Override
    public void dispose() {
        setVisible(false); //ensure overlay hidden when disposing
        super.dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.isVisible() == visible) { return; }

        if (visible) {
            setLocationRelativeTo(JOptionPane.getRootFrame());
            if (isModal()) {
                SOverlayUtils.showOverlay();
            }
        }
        else if (isModal()) {
            SOverlayUtils.hideOverlay();
        }
        super.setVisible(visible);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        if (this.titleBar != null) {
            this.titleBar.setTitle(title);
        }
    }

    @Override
    public void setIconImage(Image image) {
        super.setIconImage(image);
        if (this.titleBar != null) {
            this.titleBar.setIconImage(image);
        }
    }

    //relay certain methods to the inner panel if it has been initialized
    @Override
    public void setContentPane(Container contentPane) {
        if (innerPanel != null) {
            innerPanel.add(contentPane, "w 100%!, h 100%!");
        }
        super.setContentPane(contentPane);
    }

    @Override
    public Component add(Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(comp);
        }
        return super.add(comp);
    }
    
    @Override
    public void add(PopupMenu popup) {
        if (innerPanel != null) {
            innerPanel.add(popup);
            return;
        }
        super.add(popup);
    }
    
    @Override
    public void add(Component comp, Object constraints) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints);
            return;
        }
        super.add(comp, constraints);
    }
    
    @Override
    public Component add(Component comp, int index) {
        if (innerPanel != null) {
            return innerPanel.add(comp, index);
        }
        return super.add(comp, index);
    }
    
    @Override
    public void add(Component comp, Object constraints, int index) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints, index);
            return;
        }
        super.add(comp, constraints, index);
    }
    
    @Override
    public Component add(String name, Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(name, comp);
        }
        return super.add(name, comp);
    }

    private void addMoveSupport() {
        this.titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 1) {
                        locBeforeMove = getLocation();
                        mouseDownLoc = e.getLocationOnScreen();
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    locBeforeMove = null;
                    mouseDownLoc = null;
                }
            }
        });
        this.titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseDownLoc != null) {
                    final Point loc = e.getLocationOnScreen();
                    final int dx = loc.x - mouseDownLoc.x;
                    final int dy = loc.y - mouseDownLoc.y;
                    setLocation(locBeforeMove.x + dx, locBeforeMove.y + dy);
                }
            }
        });
    }

    @Override
    public boolean isMinimized() {
        return false;
    }

    @Override
    public void setMinimized(boolean b) {
    }

    @Override
    public boolean isMaximized() {
        return false;
    }

    @Override
    public void setMaximized(boolean b) {
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void setFullScreen(boolean b) {
    }

    @Override
    public boolean getLockTitleBar() {
        return false;
    }

    @Override
    public void setLockTitleBar(boolean b) {
    }

    @Override
    public Image getIconImage() {
        return getIconImages().isEmpty() ? null : getIconImages().get(0);
    }
}