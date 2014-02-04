package forge.game.ability.effects;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.card.mana.ManaCostShard;
import forge.game.GameActionUtil;
import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CounterType;
import forge.game.player.Player;
import forge.game.spellability.AbilityManaPart;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;

public class ManaEffect extends SpellAbilityEffect {

    @Override
    public void resolve(SpellAbility sa) {
        final Card card = sa.getSourceCard();

        AbilityManaPart abMana = sa.getManaPart();

        // Spells are not undoable
        sa.setUndoable(sa.isAbility() && sa.isUndoable());

        final List<Player> tgtPlayers = getTargetPlayers(sa);
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final boolean optional = sa.hasParam("Optional");
        final Game game = sa.getActivatingPlayer().getGame();

        if (optional && !sa.getActivatingPlayer().getController().confirmAction(sa, null, "Do you want to add mana to your mana pool?")) {
            return;
        }
        if (abMana.isComboMana()) {
            for (Player p : tgtPlayers) {
                int amount = sa.hasParam("Amount") ? AbilityUtils.calculateAmount(card, sa.getParam("Amount"), sa) : 1;
                if (tgt == null || p.canBeTargetedBy(sa)) {
                    Player activator = sa.getActivatingPlayer();
                    //String colorsNeeded = abMana.getExpressChoice();
                    String[] colorsProduced = abMana.getComboColors().split(" ");
                    
                    
                    final StringBuilder choiceString = new StringBuilder();
                    ColorSet colorOptions = null;
                    if (!abMana.isAnyMana()) {
                        colorOptions = ColorSet.fromNames(colorsProduced);
                    }
                    else {
                        colorOptions = ColorSet.fromNames(MagicColor.Constant.ONLY_COLORS);
                    }
                    for (int nMana = 1; nMana <= amount; nMana++) {
                        String choice = "";
                        byte chosenColor = activator.getController().chooseColor("Select Mana to Produce", sa, colorOptions);
                        if (chosenColor == 0)
                            throw new RuntimeException("ManaEffect::resolve() /*combo mana*/ - " + activator + " color mana choice is empty for " + card.getName());

                        choice = MagicColor.toShortString(chosenColor);
                        if (nMana != 1) {
                            choiceString.append(" ");
                        }
                        choiceString.append(choice);
                    }
                    game.action.nofityOfValue(sa, card, activator + " picked " + choiceString, activator);
                    abMana.setExpressChoice(choiceString.toString());
                }
            }
        }
        else if (abMana.isAnyMana()) {
            for (Player p : tgtPlayers) {
                if (tgt == null || p.canBeTargetedBy(sa)) {
                    Player act = sa.getActivatingPlayer();
                    // AI color choice is set in ComputerUtils so only human players need to make a choice
    
                    String colorsNeeded = abMana.getExpressChoice();
                    String choice = "";

                    ColorSet colorMenu = null;
                    byte mask = 0;
                    //loop through colors to make menu
                    for (int nChar = 0; nChar < colorsNeeded.length(); nChar++) {
                        mask |= MagicColor.fromName(colorsNeeded.charAt(nChar));
                    }
                    colorMenu = ColorSet.fromMask(mask == 0 ? MagicColor.ALL_COLORS : mask);
                    byte val = act.getController().chooseColor("Select Mana to Produce", sa, colorMenu);
                    if (0 == val) {
                        throw new RuntimeException("ManaEffect::resolve() /*any mana*/ - " + act + " color mana choice is empty for " + card.getName());
                    }
                    choice = MagicColor.toShortString(val);

                    game.action.nofityOfValue(sa, card, act + " picked " + choice, act);
                    abMana.setExpressChoice(choice);
                }
            }
        }
        else if (abMana.isSpecialMana()) {
            for (Player p : tgtPlayers) {
                if (tgt == null || p.canBeTargetedBy(sa)) {
                    String type = abMana.getOrigProduced().split("Special ")[1];

                    if (type.equals("EnchantedManaCost")) {
                        Card enchanted = card.getEnchantingCard();
                        if (enchanted == null ) 
                            continue;

                        StringBuilder sb = new StringBuilder();
                        int generic = enchanted.getManaCost().getGenericCost();
                        if( generic > 0 )
                            sb.append(generic);

                        for (ManaCostShard s : enchanted.getManaCost()) {
                            ColorSet cs = ColorSet.fromMask(s.getColorMask());
                            if(cs.isColorless())
                                continue;
                            sb.append(' ');
                            if (cs.isMonoColor())
                                sb.append(MagicColor.toShortString(s.getColorMask()));
                            else /* (cs.isMulticolor()) */ {
                                byte chosenColor = sa.getActivatingPlayer().getController().chooseColor("Choose a single color from " + s.toString(), sa, cs);
                                sb.append(MagicColor.toShortString(chosenColor));
                            }
                        }
                        abMana.setExpressChoice(sb.toString().trim());
                    }

                    if (abMana.getExpressChoice().isEmpty()) {
                        System.out.println("AbilityFactoryMana::manaResolve() - special mana effect is empty for " + sa.getSourceCard().getName());
                    }
                }
            }    
        }

        for (final Player player : tgtPlayers) {
            abMana.produceMana(GameActionUtil.generatedMana(sa), player, sa);
        }

        // Only clear express choice after mana has been produced
        abMana.clearExpressChoice();

        // convert these to SubAbilities when appropriate
        if (sa.hasParam("Stuck")) {
            sa.setUndoable(false);
            card.addHiddenExtrinsicKeyword("This card doesn't untap during your next untap step.");
        }

        final String deplete = sa.getParam("Deplete");
        if (deplete != null) {
            final int num = card.getCounters(CounterType.getType(deplete));
            if (num == 0) {
                sa.setUndoable(false);
                game.getAction().sacrifice(card, null);
            }
        }

        //resolveDrawback(sa);
    }

    /**
     * <p>
     * manaStackDescription.
     * </p>
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param abMana
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @param af
     *            a {@link forge.game.ability.AbilityFactory} object.
     * 
     * @return a {@link java.lang.String} object.
     */

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        String mana = !sa.hasParam("Amount") || StringUtils.isNumeric(sa.getParam("Amount"))
                ? GameActionUtil.generatedMana(sa) : "mana";
        sb.append("Add ").append(mana).append(" to your mana pool.");
        return sb.toString();
    }
}
