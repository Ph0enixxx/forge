package forge.card.abilityfactory.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import forge.Card;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;

/**
     * <p>
     * digUntilStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    
public class DigUntilEffect extends SpellEffect {
    

    /* (non-Javadoc)
         * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
         */
    @Override
    protected String getStackDescription(Map<String, String> params, SpellAbility sa) {
        final Card host = sa.getSourceCard();
        final StringBuilder sb = new StringBuilder();
    
        String desc = "Card";
        if (params.containsKey("ValidDescription")) {
            desc = params.get("ValidDescription");
        }
    
        int untilAmount = 1;
        if (params.containsKey("Amount")) {
            untilAmount = AbilityFactory.calculateAmount(sa.getAbilityFactory().getHostCard(), params.get("Amount"), sa);
        }
    
        if (!(sa instanceof AbilitySub)) {
            sb.append(host).append(" - ");
        } else {
            sb.append(" ");
        }
    
        ArrayList<Player> tgtPlayers;
    
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }
    
        for (final Player pl : tgtPlayers) {
            sb.append(pl).append(" ");
        }
    
        sb.append("reveals cards from his or her library until revealing ");
        sb.append(untilAmount).append(" ").append(desc).append(" card");
        if (untilAmount != 1) {
            sb.append("s");
        }
        sb.append(". Put ");
    
        final ZoneType found = ZoneType.smartValueOf(params.get("FoundDestination"));
        final ZoneType revealed = ZoneType.smartValueOf(params.get("RevealedDestination"));
        if (found != null) {
    
            sb.append(untilAmount > 1 ? "those cards" : "that card");
            sb.append(" ");
    
            if (found.equals(ZoneType.Hand)) {
                sb.append("into his or her hand ");
            }
    
            if (revealed.equals(ZoneType.Graveyard)) {
                sb.append("and all other cards into his or her graveyard.");
            }
            if (revealed.equals(ZoneType.Exile)) {
                sb.append("and exile all other cards revealed this way.");
            }
        } else {
            if (revealed.equals(ZoneType.Hand)) {
                sb.append("all cards revealed this way into his or her hand");
            }
        }
        return sb.toString();
    }

    /**
     * <p>
     * digUntilResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    @Override
    public void resolve(java.util.Map<String,String> params, SpellAbility sa) {
        final Card host = sa.getSourceCard();

        String type = "Card";
        if (params.containsKey("Valid")) {
            type = params.get("Valid");
        }

        int untilAmount = 1;
        if (params.containsKey("Amount")) {
            untilAmount = AbilityFactory.calculateAmount(host, params.get("Amount"), sa);
        }

        Integer maxRevealed = null;
        if (params.containsKey("MaxRevealed")) {
            maxRevealed = AbilityFactory.calculateAmount(host, params.get("MaxRevealed"), sa);
        }

        final boolean remember = params.containsKey("RememberFound");

        ArrayList<Player> tgtPlayers;

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(host, params.get("Defined"), sa);
        }

        final ZoneType foundDest = ZoneType.smartValueOf(params.get("FoundDestination"));
        final int foundLibPos = AbilityFactory.calculateAmount(host, params.get("FoundLibraryPosition"), sa);
        final ZoneType revealedDest = ZoneType.smartValueOf(params.get("RevealedDestination"));
        final int revealedLibPos = AbilityFactory.calculateAmount(host, params.get("RevealedLibraryPosition"), sa);

        for (final Player p : tgtPlayers) {
            if ((tgt == null) || p.canBeTargetedBy(sa)) {
                final List<Card> found = new ArrayList<Card>();
                final List<Card> revealed = new ArrayList<Card>();

                final PlayerZone library = p.getZone(ZoneType.Library);

                final int maxToDig = maxRevealed != null ? maxRevealed : library.size();

                for (int i = 0; i < maxToDig; i++) {
                    final Card c = library.get(i);
                    revealed.add(c);
                    if (c.isValid(type, sa.getActivatingPlayer(), host)) {
                        found.add(c);
                        if (remember) {
                            host.addRemembered(c);
                        }
                        if (found.size() == untilAmount) {
                            break;
                        }
                    }
                }

                if (revealed.size() > 0) {
                    GuiChoose.one(p + " revealed: ", revealed);
                }

                // TODO Allow Human to choose the order
                if (foundDest != null) {
                    final Iterator<Card> itr = found.iterator();
                    while (itr.hasNext()) {
                        final Card c = itr.next();
                        if (params.containsKey("GainControl") && foundDest.equals(ZoneType.Battlefield)) {
                            c.addController(sa.getAbilityFactory().getHostCard());
                            Singletons.getModel().getGame().getAction().moveTo(c.getController().getZone(foundDest), c);
                        } else {
                            Singletons.getModel().getGame().getAction().moveTo(foundDest, c, foundLibPos);
                        }
                        revealed.remove(c);
                    }
                }

                if (params.containsKey("RememberRevealed")) {
                    for (final Card c : revealed) {
                        host.addRemembered(c);
                    }
                }

                final Iterator<Card> itr = revealed.iterator();
                while (itr.hasNext()) {
                    final Card c = itr.next();
                    Singletons.getModel().getGame().getAction().moveTo(revealedDest, c, revealedLibPos);
                }

                if (params.containsKey("Shuffle")) {
                    p.shuffle();
                }
            } // end foreach player
        }
    } // end resolve

    // **********************************************************************
    // ******************************* RevealHand ***************************
    // **********************************************************************


    /**
     * <p>
     * revealHandCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
}