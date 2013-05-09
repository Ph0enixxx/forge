package forge.card.ability.ai;


import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.player.AIPlayer;

public class ManaEffectAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(AIPlayer aiPlayer, SpellAbility sa) {
        return false;
    }

    /**
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     * 
     * @return a boolean.
     */
    @Override
    protected boolean doTriggerAINoCost(AIPlayer aiPlayer, SpellAbility sa, boolean mandatory) {
        return true;
    }

}