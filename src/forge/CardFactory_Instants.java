package forge;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;


public class CardFactory_Instants {

	private static final int hasKeyword(Card c, String k) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith(k)) return i;
        
        return -1;
    }
    
    public static Card getCard(final Card card, final String cardName, String owner) {
    	
    	
        //*************** START *********** START **************************
        if (cardName.equals("Resuscitate")) {
            /**
             *  This card does not work and this is a place holder.
             *  May require a keyword factory.
             */
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2024445242584858534L;
                
                @Override
                public void resolve() {
                    
                }//resolve
            };//SpellAbility
            spell.setStackDescription(card.getName() + " - I do nothing but go to the graveyard.");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
    
    	 //*************** START *********** START **************************
        else if (cardName.equals("Brave the Elements")) {
        	/**
        	 *  This card now works slightly better than it did before the spAllPump 
        	 *  keyword was created. The AI is too simple and needs some work.
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -7998437920995642451L;
				
				@Override
                public boolean canPlayAI() {
                    return getAttacker() != null;
                }
				
				public Card getAttacker() {
                    // target creatures that is going to attack
                    Combat c = ComputerUtil.getAttackers();
                    Card[] att = c.getAttackers();

                    // Effect best used on at least a couple creatures
                    if (att.length > 1) {
                        return att[0];
                    } else return null;
                }//getAttacker()
				
				String getKeywordBoost() {
					String theColor = getChosenColor();
					return "Protection from " + theColor;
                }//getKeywordBoost()
				
				String getChosenColor() {
	                   // Choose color for protection in Brave the Elements
	             	   String color = "";
	             	   if (card.getController().equals(Constant.Player.Human)) {

	             		   // String[] colors = Constant.Color.Colors;
	             		   // colors[colors.length-1] = null;
	             		   
	             		   // You can no longer choose to gain "protection from null".
	             		   String[] colors = Constant.Color.onlyColors;

	             		   Object o = AllZone.Display.getChoice("Choose color", colors);
	             		   color = (String)o;
	             	   }
	             	   else {
	             		   PlayerZone lib = AllZone.getZone(Constant.Zone.Library, Constant.Player.Human);
	             		   PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human);
	             		   CardList list = new CardList();
	             		   list.addAll(lib.getCards());
	             		   list.addAll(hand.getCards());

	             		   if (list.size() > 0) {  
	             			   String mpcolor = CardFactoryUtil.getMostProminentColor(list);
	             			   if (!mpcolor.equals(""))
	             				   color = mpcolor;
	             			   else
	             				   color = "black";
	             		   }
	             		   else  {
	             			   color = "black";
	             		   }
	             	   }
	             	   return color;
	                } // getChosenColor
				
				@Override
				public void resolve() {
					final String kboost = getKeywordBoost();
					
					CardList list = new CardList();
					PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    list.addAll(play.getCards());
                    list = list.filter(new CardListFilter() {
                       public boolean addCard(Card c) {
                          return CardUtil.getColors(c).contains(Constant.Color.White);
                       }
                    });
                    
                    for (int i = 0; i < list.size(); i++) {
                        final Card[] target = new Card[1];
                        target[0] = list.get(i);
                        
                        final Command untilEOT = new Command() {
							private static final long serialVersionUID = 6308754740309909072L;

							public void execute() {
                                if (AllZone.GameAction.isCardInPlay(target[0])) {
                                	target[0].removeExtrinsicKeyword(kboost);
                                }
                            }
                        };//Command
                        
                        if (AllZone.GameAction.isCardInPlay(target[0]) && 
                        		!target[0].getKeyword().contains(kboost)) {
                            target[0].addExtrinsicKeyword(kboost);
                            
                            AllZone.EndOfTurn.addUntil(untilEOT);
                        }//if
                    }//for
				}//resolve
        	};//SpellAbility
        	card.setSVar("PlayMain1", "TRUE");
        	card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Pongify")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7657135492744577568L;
                
                @Override
                public boolean canPlayAI() {
                    return (getCreature().size() != 0) && (AllZone.Phase.getTurn() > 4);
                }
                
                @Override
                public void chooseTargetAI() {
                    Card best = CardFactoryUtil.AI_getBestCreature(getCreature());
                    setTargetCard(best);
                }
                
                CardList getCreature() {
                    CardList list = CardFactoryUtil.AI_getHumanCreature(card, true);
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (3 < c.getNetAttack());
                        }
                    });
                    return list;
                }//getCreature()
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        CardFactoryUtil.makeToken("Ape", "G 3 3 Ape", getTargetCard().getController(), "G",
                                new String[] {"Creature", "Ape"}, 3, 3, new String[] {""});
                        AllZone.GameAction.destroyNoRegeneration(getTargetCard());
                    }
                }//resolve()
            };//SpellAbility
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Peel from Reality")) {
            final Card[] target = new Card[2];
            final int[] index = new int[1];
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5781099237509350795L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    //bounce two creatures in target[]
                    for(int i = 0; i < target.length; i++) {
                        Card c = target[i];
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, c.getOwner());
                        
                        if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) AllZone.GameAction.moveTo(
                                hand, c);
                    }
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                private static final long serialVersionUID = -5897481915350104062L;
                
                @Override
                public void showMessage() {
                    if(index[0] == 0) AllZone.Display.showMessage("Select target creature you control to bounce to your hand");
                    else AllZone.Display.showMessage("Select target creature you don't control to return to its owner's hand");
                    
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    //must target creature you control
                    if(index[0] == 0 && !c.getController().equals(card.getController())) return;
                    
                    //must target creature you don't control
                    if(index[0] == 1 && c.getController().equals(card.getController())) return;
                    

                    if(c.isCreature() && zone.is(Constant.Zone.Play) && CardFactoryUtil.canTarget(card, c)) {
                        target[index[0]] = c;
                        index[0]++;
                        showMessage();
                        
                        if(index[0] == target.length) {
                            if(this.isFree()) {
                                this.setFree(false);
                                AllZone.Stack.add(spell);
                                stop();
                            } else stopSetNext(new Input_PayManaCost(spell));
                        }
                    }
                }//selectCard()
            };//Input
            
            Input runtime = new Input() {
                private static final long serialVersionUID = 1194864613104644447L;
                
                @Override
                public void showMessage() {
                    index[0] = 0;
                    stopSetNext(input);
                }
            };//Input
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Wings of Velis Vel")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5744842090293912606L;
                
                @Override
                public boolean canPlayAI() {
                    CardList small = new CardList(AllZone.Computer_Play.getCards());
                    small = small.getType("Creature");
                    
                    //try to make a good attacker
                    if(0 < small.size()) {
                        CardListUtil.sortAttackLowFirst(small);
                        setTargetCard(small.get(0));
                        
                        return true && AllZone.Phase.getPhase().equals(Constant.Phase.Main1);
                    }
                    
                    return false;
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    //in case ability is played twice
                    final int[] oldAttack = new int[1];
                    final int[] oldDefense = new int[1];
                    
                    final Card card[] = new Card[1];
                    card[0] = getTargetCard();
                    
                    oldAttack[0] = card[0].getBaseAttack();
                    oldDefense[0] = card[0].getBaseDefense();
                    
                    card[0].setBaseAttack(4);
                    card[0].setBaseDefense(4);
                    card[0].addExtrinsicKeyword("Flying");
                    
                    //EOT
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = 7236360479349324099L;
                        
                        public void execute() {
                            card[0].setBaseAttack(oldAttack[0]);
                            card[0].setBaseDefense(oldDefense[0]);
                            
                            card[0].removeExtrinsicKeyword("Flying");
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(untilEOT);
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        
/* converted to spBounceTgt keyword
        //*************** START *********** START **************************
        else if(cardName.equals("Swords to Plowshares")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4752934806606319269L;
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        //add life
                        String player = getTargetCard().getController();
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.addLife(getTargetCard().getNetAttack());
                        
                        //remove card from play
                        AllZone.GameAction.removeFromGame(getTargetCard());
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    CardList creature = new CardList(AllZone.Human_Play.getCards());
                    creature = creature.getType("Creature");
                    creature = creature.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    return creature.size() != 0 && (AllZone.Phase.getTurn() > 4);
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList play = new CardList(AllZone.Human_Play.getCards());
                    Card target = CardFactoryUtil.AI_getBestCreature(play, card);
                    if(target != null) setTargetCard(target);
                }
            };
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
*/
        

        //*************** START *********** START **************************
        else if(cardName.equals("Crib Swap")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4567382566960071562L;
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                    	String controller = getTargetCard().getController();
                    	
                        AllZone.GameAction.removeFromGame(getTargetCard());
                        
                        CardFactoryUtil.makeToken("Shapeshifter", "C 1 1 Shapeshifter",
                                controller, "", new String[] {"Creature", "Shapeshifter"}, 1,
                                1, new String[] {"Changeling"});
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	return (getCreature().size() != 0) && (AllZone.Phase.getTurn() > 4);
                }//canPlayAI()
                
                CardList getCreature() {
                	CardList list = CardFactoryUtil.AI_getHumanCreature(card, true);
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (c.getNetAttack() > 2 
                            		&& CardFactoryUtil.canTarget(card, c) 
                            		&& (!c.getName().equals("Shapeshifter") 
                            		|| (c.getName().equals("Shapeshifter") 
                            		&& (c.isEnchanted() 
                            		|| c.getCounters(Counters.P1P1) != 0))));
                        }
                    });
                    return list;
                }//getCreature()

                @Override
                public void chooseTargetAI() {
                	Card best = CardFactoryUtil.AI_getBestCreature(getCreature());
                    setTargetCard(best);
                }//chooseTargetAI()
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            
            card.setSVar("PlayMain1", "TRUE");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
 


        
        //*************** START *********** START **************************
        else if(cardName.equals("Entomb")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4724906962713222211L;
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void humanResolve() {
                    Object check = AllZone.Display.getChoiceOptional("Select card",
                            AllZone.Human_Library.getCards());
                    if(check != null) {
                        PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                        AllZone.GameAction.moveTo(grave, (Card) check);
                    }
                    AllZone.GameAction.shuffle(Constant.Player.Human);
                }
                
                public void computerResolve() {
                    Card[] library = AllZone.Computer_Library.getCards();
                    CardList list = new CardList(library);
                    

                    //pick best creature
                    Card c = CardFactoryUtil.AI_getBestCreature(list);
                    if(c == null) c = library[0];
                    //System.out.println("comptuer picked - " +c);
                    AllZone.Computer_Library.remove(c);
                    AllZone.Computer_Graveyard.add(c);
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    return library.getCards().length != 0;
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList creature = new CardList();
                    creature.addAll(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    return creature.size() != 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Beacon of Destruction")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6653675303299939465L;
                
                @Override
                public void resolve() {
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            getTargetCard().addDamage(5, card);
                            done();
                        } else AllZone.GameAction.moveToGraveyard(card);
                    } else {
                        AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(5,card);
                        done();
                    }
                }//resolve()
                
                void done() {
                    //shuffle card back into the library
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    AllZone.GameAction.moveTo(library,card);
                    AllZone.GameAction.shuffle(card.getController());
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHumanCreatureOrPlayer());
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(spell, true, false));
            
            card.setSVar("PlayMain1", "TRUE");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Whispers of the Muse")) {
            final SpellAbility spell_one = new Spell(card) {
                
                private static final long serialVersionUID = 8341386638247535343L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    AllZone.GameAction.drawCard(card.getController());
                }//resolve()
            };//SpellAbility
            
            final SpellAbility spell_two = new Spell(card) {
                
                private static final long serialVersionUID = -131686114078716307L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.drawCard(card.getController());
                    done();
                }//resolve()
                
                void done() {
                    //return card to the hand
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    AllZone.GameAction.moveTo(hand, card);
                }
            };//SpellAbility
            spell_two.setManaCost("5 U");
            spell_two.setAdditionalManaCost("5");
            
            spell_one.setDescription("Draw a card.");
            spell_one.setStackDescription(cardName + " - " + card.getController() + " draws a card.");
            spell_two.setDescription("Buyback 5 (You may pay an additional 5 as you cast this spell. If you do, put this card into your hand as it resolves.)");
            // spell_two.setDescription("Buyback 5 - Pay 5 U , put this card into your hand as it resolves.");
            spell_two.setStackDescription(cardName + " - (Buyback) " + card.getController() + " draws a card.");
            spell_two.setIsBuyBackAbility(true);
            
            card.clearSpellAbility();
            card.addSpellAbility(spell_one);
            card.addSpellAbility(spell_two);
            
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        // TODO: Use spPumpTgt with sVar:Buyback
        else if(cardName.equals("Elvish Fury")) {
            
            final SpellAbility spell_one = new Spell(card) {
                private static final long serialVersionUID = 3356401944678089378L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    final Card[] target = new Card[1];
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = 7120352016188545025L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(target[0])) {
                                target[0].addTempAttackBoost(-2);
                                target[0].addTempDefenseBoost(-2);
                            }
                        }
                    };
                    
                    target[0] = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0])) {
                        target[0].addTempAttackBoost(2);
                        target[0].addTempDefenseBoost(2);
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    } else {

                    }
                }//resolve()
            };//SpellAbility
            
            final Card crd = card;
            
            final SpellAbility spell_two = new Spell(card) {
                private static final long serialVersionUID = 3898017438147188882L;
                
                @Override
                public boolean canPlayAI() {
                    return getAttacker() != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    setTargetCard(getAttacker());
                }
                
                public Card getAttacker() {
                    //target creature that is going to attack
                    Combat c = ComputerUtil.getAttackers();
                    
                    CardList list = new CardList(c.getAttackers());
                    CardListUtil.sortFlying(list);
                    
                    Card[] att = list.toArray();
                    if(att.length != 0) return att[0];
                    else return null;
                }//getAttacker()
                

                @Override
                public void resolve() {
                    final Card[] target = new Card[1];
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = 280295105716586978L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(target[0])) {
                                target[0].addTempAttackBoost(-2);
                                target[0].addTempDefenseBoost(-2);
                            }
                        }
                    };
                    
                    target[0] = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0])) {
                        target[0].addTempAttackBoost(2);
                        target[0].addTempDefenseBoost(2);
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    } else {
                        crd.clearReplaceMoveToGraveyardCommandList();
                    }
                }//resolve()
            };//SpellAbility
            spell_two.setManaCost("4 G");
            spell_two.setAdditionalManaCost("4");
            
            spell_one.setDescription("Target creature gets +2/+2 until end of turn.");
            spell_two.setDescription("Buyback 4 (You may pay an additional 4 as you cast this spell. If you do, put this card into your hand as it resolves.)");
            // spell_two.setDescription("Buyback 4 - Pay 4G, put this card into your hand as it resolves.");
            
            spell_one.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell_one));
            spell_two.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell_two));
            
            spell_two.setIsBuyBackAbility(true);
            
            card.clearSpellAbility();
            card.addSpellAbility(spell_one);
            card.addSpellAbility(spell_two);
            
            card.setSVar("PlayMain1", "TRUE");
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sprout Swarm")) {
            final SpellAbility spell_one = new Spell(card) {
                private static final long serialVersionUID = -609007714604161377L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Saproling", "G 1 1 Saproling", card, "G", new String[] {
                            "Creature", "Saproling"}, 1, 1, new String[] {""});
                }//resolve()
            };//SpellAbility
            
            final SpellAbility spell_two = new Spell(card) {
                private static final long serialVersionUID = -1387385820860395676L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Saproling", "G 1 1 Saproling", card, "G", new String[] {
                            "Creature", "Saproling"}, 1, 1, new String[] {""});
                    //return card to the hand
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    AllZone.GameAction.moveTo(hand, card);
                }
            };//SpellAbility
            
            spell_one.setManaCost("1 G");
            spell_two.setManaCost("4 G");
            spell_two.setAdditionalManaCost("3");
            
            spell_one.setDescription("Put a 1/1 green Saproling token into play.");
            spell_two.setDescription("Buyback 3 (You may pay an additional 3 as you cast this spell. If you do, put this card into your hand as it resolves.)");
            // spell_two.setDescription("Buyback 3 - Pay 4G, put this card into your hand as it resolves.");
            
            spell_one.setStackDescription("Sprout Swarm - Put a 1/1 green Saproling token into play");
            spell_two.setStackDescription("Sprout Swarm - Buyback, Put a 1/1 green Saproling token into play");
            
            spell_two.setIsBuyBackAbility(true);
            
            card.clearSpellAbility();
            card.addSpellAbility(spell_one);
            card.addSpellAbility(spell_two);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Ensnare")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5170378205496330425L;
                
                @Override
                public void resolve() {
                    CardList creats = new CardList();
                    creats.addAll(AllZone.Human_Play.getCards());
                    creats.addAll(AllZone.Computer_Play.getCards());
                    creats = creats.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature();
                        }
                    });
                    for(int i = 0; i < creats.size(); i++)
                        creats.get(i).tap();
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            spell.setDescription("Tap all creatures.");
            spell.setStackDescription(card.getName() + " - Tap all creatures");
            
            final SpellAbility bounce = new Spell(card) {
                private static final long serialVersionUID = 6331598238749406160L;
                
                @Override
                public void resolve() {
                    CardList creats = new CardList();
                    creats.addAll(AllZone.Human_Play.getCards());
                    creats.addAll(AllZone.Computer_Play.getCards());
                    creats = creats.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature();
                        }
                    });
                    for(int i = 0; i < creats.size(); i++)
                        creats.get(i).tap();
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Island");
                    return list.size() >= 2;
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
            };
            bounce.setDescription("You may return two Islands you control to their owner's hand rather than pay Ensnare's mana cost.");
            bounce.setStackDescription(card.getName() + " - Tap all creatures.");
            bounce.setManaCost("0");
            
            final Input bounceIslands = new Input() {
                private static final long serialVersionUID = -8511915834608321343L;
                int                       stop             = 2;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select an Island");
                    ButtonUtil.disableAll();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(c.getType().contains("Island") && zone.is(Constant.Zone.Play)) {
                        AllZone.GameAction.moveToHand(c);
                        
                        count++;
                        if(count == stop) {
                            AllZone.Stack.add(bounce);
                            stop();
                        }
                    }
                }//selectCard()
            };
            
            bounce.setBeforePayMana(bounceIslands);
            
            Command bounceIslandsAI = new Command() {
                private static final long serialVersionUID = 6399831162328201755L;
                
                public void execute() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Island");
                    //TODO: sort by tapped
                    
                    for(int i = 0; i < 2; i++) {
                        AllZone.GameAction.moveToHand(list.get(i));
                    }
                }
            };
            
            bounce.setBeforePayManaAI(bounceIslandsAI);
            
            card.clearSpellAbility();
            card.addSpellAbility(bounce);
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Raise the Alarm")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 3022771853846089829L;
                
                @Override
                public void resolve() {
                    for(int i = 0; i < 2; i++)
                        CardFactoryUtil.makeToken("Soldier", "W 1 1 Soldier", card, "W", new String[] {
                                "Creature", "Soldier"}, 1, 1, new String[] {""});
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Hunting Pack")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 143904782338241969L;
                @Override                           
                public boolean canPlayAI() {
                    return AllZone.Phase.getPhase().equals(Constant.Phase.Main2);
                }
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Beast", "G 4 4 Beast", card, "G", new String[] {
                            "Creature", "Beast"}, 4, 4, new String[] {""});                                    
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Astral Steel")) {
           final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 141478784123241969L;
                
                @Override                
                public boolean canPlay() {
                	String player = AllZone.Phase.getActivePlayer();
            		String opponent = AllZone.GameAction.getOpponent(player);
                    PlayerZone PlayerPlayZone = AllZone.getZone(Constant.Zone.Play, player);
            		PlayerZone opponentPlayZone = AllZone.getZone(Constant.Zone.Play, opponent);                  
                    CardList start = new CardList(PlayerPlayZone.getCards());
                    CardList start2 = new CardList(opponentPlayZone.getCards());
                    final CardList list = start.getType("Creature");
                    final CardList list2 = start2.getType("Creature");
                    return (list.size() + list2.size() > 0);
                }
                
                public boolean canPlayAI() {
                    return getAttacker() != null && AllZone.Phase.getPhase().equals(Constant.Phase.Main1);
                }
                
                
                @Override
                public void chooseTargetAI() {
                    setTargetCard(getAttacker());
                }
                
                public Card getAttacker() {
					String Computer = AllZone.Phase.getActivePlayer();
					PlayerZone ComputerPlayZone = AllZone.getZone(Constant.Zone.Play, Computer);
			        CardList ComputerCreatureList = new CardList(ComputerPlayZone.getCards());
			        ComputerCreatureList = ComputerCreatureList.getType("Creature");
			        ComputerCreatureList = ComputerCreatureList.filter(new CardListFilter() {
						public boolean addCard(Card c) {
							return c.getNetAttack() >= 2 && CardFactoryUtil.canTarget(card, getTargetCard());
						}
					});
                    if(ComputerCreatureList.size() != 0){
                        Card[] Target = new Card[ComputerCreatureList.size()];
            			for(int i = 0; i < ComputerCreatureList.size(); i++) {
            				Card crd = ComputerCreatureList.get(i);
            				Target[i] = crd;
            			}
    			        Random randomGenerator = new Random();
  			          int randomInt = randomGenerator.nextInt(ComputerCreatureList.size());
                    	return Target[randomInt];
                    }
                    else return null;
                }//getAttacker()              
                
                @Override
                public void resolve() {
                    final Card c = getTargetCard();
                        c.addTempAttackBoost(1);
                        c.addTempDefenseBoost(2);
                    
                    c.updateObservers();
                    
                    Command untilEOT = new Command() {
                        private static final long serialVersionUID = -28032591440730370L;
                        
                        public void execute() {
                            c.addTempAttackBoost(-1);
                            c.addTempDefenseBoost(-2);
                        }
                    };
                    AllZone.EndOfTurn.addUntil(untilEOT);
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Scattershot")) {
           final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 74777841291969L;
                
                @Override                           
                public boolean canPlayAI() {
                    return false;
                }      
                @Override
                public void resolve() {

                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) getTargetCard().addDamage(1,card);
                    }
                    };
           };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        	spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END ************************** 
        
      //*************** START *********** START **************************
        else if(cardName.equals("Reaping the Graves")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -57014445262924814L;
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    PlayerZone grave = AllZone.getZone(c);
                    
                    if(AllZone.GameAction.isCardInZone(c, grave)) {
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, c.getController());
                        AllZone.GameAction.moveTo(hand, c);
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return super.canPlay() && getCreatures().length != 0;
                }
                
                public Card[] getCreatures() {
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature");
                    return creature.toArray();
                }
                
                @Override
                public void chooseTargetAI() {
                    Card c[] = getCreatures();
                    Card biggest = c[0];
                    for(int i = 0; i < c.length; i++)
                        if(biggest.getNetAttack() < c[i].getNetAttack()) biggest = c[i];
                    
                    setTargetCard(biggest);
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            Input target = new Input() {
                private static final long serialVersionUID = -3717723884199321767L;
                
                @Override
                public void showMessage() {
                    Object check = AllZone.Display.getChoiceOptional("Select creature", getCreatures());
                    if(check != null) {
                        spell.setTargetCard((Card) check);
                        stopSetNext(new Input_PayManaCost(spell));
                    } else stop();
                }//showMessage()
                
                public Card[] getCreatures() {
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature");
                    return creature.toArray();
                }
            };//Input
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sprouting Vines")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -65984152637468746L;

				@Override
                public void resolve() {
					AllZone.GameAction.searchLibraryBasicLand(card.getController(), 
							Constant.Zone.Hand, false);
				}
                
                public boolean canPlayAI()
                {
                	PlayerZone library = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                	CardList list = new CardList(library.getCards());
                	list = list.getType("Basic");
                	return list.size() > Phase.StormCount;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END ************************** 
        
        //*************** START *********** START **************************
        else if(cardName.equals("Reiterate")) {
            final SpellAbility spell_one = new Spell(card) {

				private static final long serialVersionUID = -659841515428746L;

				@Override
                public void resolve() {
					AllZone.CardFactory.copySpellontoStack(card,getTargetCard(), true);					
				}
                
                public boolean canPlay()
                {
                	ArrayList<Card> list = AllZone.Stack.getSourceCards();
                	CardList StackList = new CardList();
                	for(int i = 0; i < list.size(); i++) StackList.add(list.get(i));

                	StackList = StackList.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return c.isSorcery() || c.isInstant();
                    	}
                    });
                	return StackList.size() > 0 && super.canPlay();
                }
            };//SpellAbility
            
            final SpellAbility spell_two = new Spell(card) {
                
                private static final long serialVersionUID = -131686114078716307L;
   				@Override
                    public void resolve() {
   					AllZone.CardFactory.copySpellontoStack(card,getTargetCard(), true);
                        done();
                }//resolve()
    				
                    
                    public boolean canPlay()
                    {
                    	ArrayList<Card> list = AllZone.Stack.getSourceCards();
                    	CardList StackList = new CardList();
                    	for(int i = 0; i < list.size(); i++) StackList.add(list.get(i));

                    	StackList = StackList.filter(new CardListFilter() {
                        	public boolean addCard(Card c) {
                        		return c.isSorcery() || c.isInstant();
                        	}
                        });
                    	return StackList.size() > 0 && super.canPlay();
                    }

                
                void done() {
                    //return card to the hand
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    AllZone.GameAction.moveTo(hand, card);
                }
            };//SpellAbility
            spell_two.setManaCost("4 R R");
            spell_two.setAdditionalManaCost("3");
            
            spell_one.setDescription("Copy target instant or sorcery spell. You may choose new targets for the copy.");
            // spell_one.setStackDescription(cardName + " - " + card.getController() + "Copies " + spell_one.getTargetCard());
            StringBuilder sbOne = new StringBuilder();
            sbOne.append(cardName).append(" - ").append(card.getController()).append(" Copies ").append(spell_one.getTargetCard());
            spell_one.setStackDescription(sbOne.toString());
            
            // spell_two.setDescription("Buyback 3 - Pay 4 R R , put this card into your hand as it resolves.");
            spell_two.setDescription("Buyback 3 (You may pay an additional 3 as you cast this spell. If you do, put this card into your hand as it resolves.)");
            // spell_two.setStackDescription(cardName + " - (Buyback) " + card.getController() + "Copies " + spell_two.getTargetCard());
            StringBuilder sbTwo = new StringBuilder();
            sbTwo.append(cardName).append(" - (Buyback) ").append(card.getController()).append(" Copies ").append(spell_two.getTargetCard());
            
            spell_two.setIsBuyBackAbility(true);
            
            Input runtime1 = new Input() {
                private static final long serialVersionUID = -7823269301012427007L;
                
                @Override
                public void showMessage() {
                	ArrayList<Card> list = AllZone.Stack.getSourceCards();
                	CardList StackList = new CardList();
                	for(int i = 0; i < list.size(); i++) StackList.add(list.get(i));

                	StackList = StackList.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card c)
                    	{
                    		return c.isSorcery() || c.isInstant();
                    	}
                    });
                    
                    stopSetNext(CardFactoryUtil.input_Spell(spell_one, StackList, false));
                    
                }//showMessage()
            };//Input
           
            Input runtime2 = new Input() {
                private static final long serialVersionUID = -7823269301012427007L;
                
                @Override
                public void showMessage() {
                	ArrayList<Card> list = AllZone.Stack.getSourceCards();
                	CardList StackList = new CardList();
                	for(int i = 0; i < list.size(); i++) StackList.add(list.get(i));

                	StackList = StackList.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card c)
                    	{
                    		return c.isSorcery() || c.isInstant();
                    	}
                    });
                    
                    stopSetNext(CardFactoryUtil.input_Spell(spell_two, StackList, false));
                    
                }//showMessage()
            };//Input
            
            card.clearSpellAbility();
            card.addSpellAbility(spell_one);
            card.addSpellAbility(spell_two);
            card.setCopiesSpells(true);
            spell_one.setBeforePayMana(runtime1);
            spell_two.setBeforePayMana(runtime2);


        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Twincast")) {
            final SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -659841515428746L;

				@Override
                public void resolve() {
					AllZone.CardFactory.copySpellontoStack(card,getTargetCard(), true);				
				}
                
                public boolean canPlay()
                {
                	ArrayList<Card> list = AllZone.Stack.getSourceCards();
                	CardList StackList = new CardList();
                	for(int i = 0; i < list.size(); i++) StackList.add(list.get(i));

                	StackList = StackList.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return c.isSorcery() || c.isInstant();
                    	}
                    });
                	return StackList.size() > 0 && super.canPlay();
                }
            };//SpellAbility
            Input runtime = new Input() {
                private static final long serialVersionUID = -7823269301012427007L;
                
                @Override
                public void showMessage() {
                	ArrayList<Card> list = AllZone.Stack.getSourceCards();
                	CardList StackList = new CardList();
                	for(int i = 0; i < list.size(); i++) StackList.add(list.get(i));

                	StackList = StackList.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card c)
                    	{
                    		return c.isSorcery() || c.isInstant();
                    	}
                    });
                    
                    stopSetNext(CardFactoryUtil.input_Spell(spell, StackList, false));
                    
                }//showMessage()
            };//Input
            card.clearSpellAbility();
            card.setCopiesSpells(true);
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Wing Shards")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4780150265170723L;
                
                @Override                
                public boolean canPlay() {
                    return (AllZone.Phase.getPhase().equals(Constant.Phase.Combat_Declare_Attackers_InstantAbility) || (AllZone.Phase.getPhase().equals(Constant.Phase.Combat_Declare_Blockers_InstantAbility)));
                }
                @Override
                public void resolve() {
                	Card attack[] = AllZone.Combat.getAttackers(); 
                	Card target = null;
                    String player = card.getController();
                    if(player != "Human"){
                        Object check = AllZone.Display.getChoiceOptional("Select creature", attack);
                        if(check != null) {
                           target = ((Card) check);
                        } 
                    } else {
                        CardList Targets = new CardList();
                        String TPlayer = AllZone.GameAction.getOpponent(card.getController());
    					PlayerZone TZone = AllZone.getZone(Constant.Zone.Play, TPlayer);
                        for(int i = 0; i < attack.length; i++) {
            				Card crd = attack[i];			                                
                                if(AllZone.GameAction.isCardInZone(attack[i], TZone)) Targets.add(crd);
                        }
                        CardListUtil.sortAttack(Targets);
                        if(Targets.size() != 0) target = (Targets.get(Targets.size() - 1));
                        }

                    if(target != null) AllZone.GameAction.sacrifice(target);                
                }
                
                @Override
                public boolean canPlayAI() {
                	Card attack[] = AllZone.Combat.getAttackers();
                    CardList Targets = new CardList();
                    for(int i = 0; i < attack.length; i++) {
        				Card crd = attack[i];
                        Targets.add(crd);
                    }
                    return (Targets.size() > 0 && AllZone.Phase.getPhase().equals(Constant.Phase.Combat_Declare_Attackers_InstantAbility)) ;
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Fact or Fiction")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1481112451519L;
                
                @Override
                public void resolve() {
                    
                    Card choice = null;
                    
                    //check for no cards in hand on resolve
                    PlayerZone Library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    PlayerZone Hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    PlayerZone Grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    CardList cards = new CardList();
                    
                    if(Library.size() == 0) {
                    	JOptionPane.showMessageDialog(null, "No more cards in library.", "", JOptionPane.INFORMATION_MESSAGE);
                    	return;
                    }
                    int Count = 5;
                    if(Library.size() < 5) Count = Library.size();
                    for(int i = 0; i < Count; i++) cards.add(Library.get(i));
                    CardList Pile1 = new CardList();
                    CardList Pile2 = new CardList();
                    boolean stop = false;
                    int  Pile1CMC = 0;
                    int  Pile2CMC = 0;
                   

                        AllZone.Display.getChoice("Revealing top " + Count + " cards of library: ", cards.toArray());
                        //Human chooses
                        if(card.getController().equals(Constant.Player.Computer)) {
                        for(int i = 0; i < Count; i++) {
                        	if(stop == false) {
                        choice = AllZone.Display.getChoiceOptional("Choose cards to put into the first pile: ", cards.toArray());
                        if(choice != null) {
                        	Pile1.add(choice);
                        	cards.remove(choice);
                        	Pile1CMC = Pile1CMC + CardUtil.getConvertedManaCost(choice);
                        }
                        else stop = true;	
                        }
                        }
                        for(int i = 0; i < Count; i++) {
                        	if(!Pile1.contains(Library.get(i))) {
                        		Pile2.add(Library.get(i));
                        		Pile2CMC = Pile2CMC + CardUtil.getConvertedManaCost(Library.get(i));
                        	}
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("You have spilt the cards into the following piles" + "\r\n" + "\r\n");
                        sb.append("Pile 1: " + "\r\n");
                        for(int i = 0; i < Pile1.size(); i++) sb.append(Pile1.get(i).getName() + "\r\n");
                        sb.append("\r\n" + "Pile 2: " + "\r\n");
                        for(int i = 0; i < Pile2.size(); i++) sb.append(Pile2.get(i).getName() + "\r\n");
                        JOptionPane.showMessageDialog(null, sb, "", JOptionPane.INFORMATION_MESSAGE);
                        if(Pile1CMC >= Pile2CMC) {
                        	JOptionPane.showMessageDialog(null, "Computer adds the first pile to its hand and puts the second pile into the graveyard", "", JOptionPane.INFORMATION_MESSAGE);
	                    	  for(int i = 0; i < Pile1.size(); i++) AllZone.GameAction.moveTo(Hand, Pile1.get(i));
	                    	  for(int i = 0; i < Pile2.size(); i++) AllZone.GameAction.moveTo(Grave, Pile2.get(i));
                        } else {
                        	JOptionPane.showMessageDialog(null, "Computer adds the second pile to its hand and puts the first pile into the graveyard", "", JOptionPane.INFORMATION_MESSAGE);
	                    	  for(int i = 0; i < Pile2.size(); i++) AllZone.GameAction.moveTo(Hand, Pile2.get(i));
	                    	  for(int i = 0; i < Pile1.size(); i++) AllZone.GameAction.moveTo(Grave, Pile1.get(i));		
		    		}
                        
                    } else//Computer chooses (It picks the highest converted mana cost card and 1 random card.)
                    {
                        Card biggest = null;
                        biggest = Library.get(0);
                        
                        for(int i = 0; i < Count; i++) {
                            if(CardUtil.getConvertedManaCost(biggest.getManaCost()) >= CardUtil.getConvertedManaCost(biggest.getManaCost())) {
                                biggest = cards.get(i);
                            }
                        }
                        Pile1.add(biggest);
                        cards.remove(biggest);
                        if(cards.size() > 0) { 
                        Card Random = CardUtil.getRandom(cards.toArray());
                        Pile1.add(Random);
                        }
                        for(int i = 0; i < Count; i++) if(!Pile1.contains(Library.get(i))) Pile2.add(Library.get(i));
                        StringBuilder sb = new StringBuilder();
                        sb.append("Choose a pile to add to your hand: " + "\r\n" + "\r\n");
                        sb.append("Pile 1: " + "\r\n");
                        for(int i = 0; i < Pile1.size(); i++) sb.append(Pile1.get(i).getName() + "\r\n");
                        sb.append("\r\n" + "Pile 2: " + "\r\n");
                        for(int i = 0; i < Pile2.size(); i++) sb.append(Pile2.get(i).getName() + "\r\n");
			        	Object[] possibleValues = {"Pile 1", "Pile 2"};
			        	Object q = JOptionPane.showOptionDialog(null, sb, "Fact or Fiction", 
			        			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
			        			null, possibleValues, possibleValues[0]);
	                      if(q.equals(0)) {	                    	 
	                    	  for(int i = 0; i < Pile1.size(); i++) AllZone.GameAction.moveTo(Hand, Pile1.get(i));
	                    	  for(int i = 0; i < Pile2.size(); i++) AllZone.GameAction.moveTo(Grave, Pile2.get(i));
			    		} else {
	                    	  for(int i = 0; i < Pile2.size(); i++) AllZone.GameAction.moveTo(Hand, Pile2.get(i));
	                    	  for(int i = 0; i < Pile1.size(); i++) AllZone.GameAction.moveTo(Grave, Pile1.get(i));	
			    		}
                    }
                   Pile1.clear();
                   Pile2.clear();
                }//resolve()
                        
                @Override
                public boolean canPlayAI() {
                	PlayerZone Library = AllZone.getZone(Constant.Zone.Library, card.getController());
                	CardList cards = new CardList(Library.getCards());
                    return cards.size() >= 10;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        //*************** START *********** START **************************
        if(cardName.equals("Brain Freeze")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4247050159744693L;
                
                @Override
                public boolean canPlayAI() {
                    String player = getTargetPlayer();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    return (libList.size() > 0 && ((AllZone.Phase.getPhase().equals(Constant.Phase.Main2)) || Phase.StormCount*3 >= libList.size()));
                }
                
                @Override
                public void resolve() {
                    String player = getTargetPlayer();
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    CardList libList = new CardList(lib.getCards());
                    
                    int max = 3;
                    if(libList.size() < max) max = libList.size();
                    
                    for(int i = 0; i < max; i++) {
                        Card c = libList.get(i);
                        lib.remove(c);
                        grave.add(c);
                    }
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************    
        
      //*************** START *********** START **************************
        else if(cardName.equals("Reach of Branches")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2723115210677439611L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Treefolk Shaman", "G 2 5 Treefolk Shaman", card, "G", new String[] {
                            "Creature", "Treefolk", "Shaman"}, 2, 5, new String[] {""});
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
    	
        //*************** START *********** START **************************
        else if(cardName.equals("Volcanic Fallout")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8274309635261086286L;
                
                @Override
                public void resolve() {
                    CardList all = new CardList();
                    all.addAll(AllZone.Human_Play.getCards());
                    all.addAll(AllZone.Computer_Play.getCards());
                    all = all.getType("Creature");
                    
                    for(int i = 0; i < all.size(); i++) {
                        if(CardFactoryUtil.canDamage(card, all.get(i))) all.get(i).addDamage(2, card);
                    }
                    
                    PlayerLife compLife = AllZone.GameAction.getPlayerLife(Constant.Player.Computer);
                    compLife.subtractLife(2,card);
                    
                    PlayerLife humLife = AllZone.GameAction.getPlayerLife(Constant.Player.Human);
                    humLife.subtractLife(2,card);
                    
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Play.getCards());
                    CardList computer = new CardList(AllZone.Computer_Play.getCards());
                    
                    human = human.getType("Creature");
                    computer = computer.getType("Creature");
                    
                    human = CardListUtil.filterToughness(human, 2);
                    computer = CardListUtil.filterToughness(computer, 2);
                    
                    PlayerLife compLife = AllZone.GameAction.getPlayerLife(Constant.Player.Computer);
                    
                    //the computer will at least destroy 2 more human creatures
                    return computer.size() < human.size() - 1 && compLife.getLife() > 3;
                }//canPlayAI()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        
        //*************** START *********** START **************************
        else if (cardName.equals("Renewed Faith")) { 
            /**
             *   The "You gain 6 life." ability is now done via a keyword. This code
             *   object will give the controller 2 life when this card is cycled.
             */
            card.addCycleCommand(new Command() {
                private static final long serialVersionUID = 7699412574052780825L;
                    
                public void execute() {
                    PlayerLife life = AllZone.GameAction.getPlayerLife(card.getController());
                    life.addLife(2);
                }
            });
        }//*************** END ************ END **************************
       

        //*************** START *********** START **************************
        else if(cardName.equals("Life Burst")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 5653342880372240806L;
                
                @Override
                public void resolve() {
                    CardList count = new CardList();
                    count.addAll(AllZone.Human_Graveyard.getCards());
                    count.addAll(AllZone.Computer_Graveyard.getCards());
                    count = count.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getName().equals("Life Burst");
                        }
                    });
                    
                    PlayerLife life = AllZone.GameAction.getPlayerLife(getTargetPlayer());
                    life.addLife(4 + (4 * count.size()));
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetComputer());
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Accumulated Knowledge")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7650377883588723237L;
                
                @Override
                public void resolve() {
                    CardList count = new CardList();
                    count.addAll(AllZone.Human_Graveyard.getCards());
                    count.addAll(AllZone.Computer_Graveyard.getCards());
                    count = count.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getName().equals("Accumulated Knowledge");
                        }
                    });
                    
                    for(int i = 0; i <= count.size(); i++) {
                        AllZone.GameAction.drawCard(card.getController());
                    }
                }
            };
            spell.setDescription("Draw a card, then draw cards equal to the number of cards named Accumulated Knowledge in all graveyards.");
            spell.setStackDescription(cardName
                    + " - Draw a card, then draw cards equal to the number of cards named Accumulated Knowledge in all graveyards.");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Echoing Decay")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 3154935854257358023L;
                
                @Override
                public boolean canPlayAI() {
                    CardList c = getCreature();
                    if(c.isEmpty()) return false;
                    else {
                        setTargetCard(c.get(0));
                        return true;
                    }
                }//canPlayAI()
                
                CardList getCreature() {
                    CardList out = new CardList();
                    CardList list = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    list.shuffle();
                    
                    for(int i = 0; i < list.size(); i++)
                        if((list.get(i).getNetAttack() >= 2) && (list.get(i).getNetDefense() <= 2)) out.add(list.get(i));
                    
                    //in case human player only has a few creatures in play, target anything
                    if(out.isEmpty() && 0 < CardFactoryUtil.AI_getHumanCreature(2, card, true).size()
                            && 3 > CardFactoryUtil.AI_getHumanCreature(card, true).size()) {
                        out.addAll(CardFactoryUtil.AI_getHumanCreature(2, card, true).toArray());
                        CardListUtil.sortFlying(out);
                    }
                    return out;
                }//getCreature()
                

                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        final Card c = getTargetCard();
                        
                        c.addTempAttackBoost(-2);
                        c.addTempDefenseBoost(-2);
                        
                        AllZone.EndOfTurn.addUntil(new Command() {
                            private static final long serialVersionUID = 1327455269456577020L;
                            
                            public void execute() {
                                c.addTempAttackBoost(2);
                                c.addTempDefenseBoost(2);
                            }
                        });
                        
                        //get all creatures
                        CardList list = new CardList();
                        list.addAll(AllZone.Human_Play.getCards());
                        list.addAll(AllZone.Computer_Play.getCards());
                        
                        list = list.getName(getTargetCard().getName());
                        list.remove(getTargetCard());
                        
                        if(!getTargetCard().isFaceDown()) for(int i = 0; i < list.size(); i++) {
                            final Card crd = list.get(i);
                            
                            crd.addTempAttackBoost(-2);
                            crd.addTempDefenseBoost(-2);
                            
                            AllZone.EndOfTurn.addUntil(new Command() {
                                private static final long serialVersionUID = 5151337777143949221L;
                                
                                public void execute() {
                                    crd.addTempAttackBoost(2);
                                    crd.addTempDefenseBoost(2);
                                }
                            });
                            //list.get(i).addDamage(2);
                        }
                        
                    }//in play?
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Hidetsugu's Second Rite")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 176857775451818523L;
                
                @Override
                public void resolve() {
                    PlayerLife life = AllZone.GameAction.getPlayerLife(getTargetPlayer());
                    if(life.getLife() == 10) life.subtractLife(10,card);
                }
                
                /*
                @Override
                public boolean canPlay() {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerLife p = AllZone.GameAction.getPlayerLife(opponent);
                    return p.getLife() == 10;
                }
                */
                
                @Override
                public boolean canPlayAI() {
                    PlayerLife humanLife = AllZone.GameAction.getPlayerLife(Constant.Player.Human);
                    return humanLife.getLife() == 10;
                }
                
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }//*************** END ************ END **************************
        


        
        //*************** START *********** START **************************
        else if(cardName.equals("Spell Pierce")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4685055135070191326L;
                
                @Override
                public void resolve() {
                    String manaCost = "2";
                    Ability ability = new Ability(card, manaCost) {
                        @Override
                        public void resolve() {
                            ;
                        }
                    };
                    
                    final Command unpaidCommand = new Command() {
                        private static final long serialVersionUID = 8094833091127334678L;
                        
                        public void execute() {
                            SpellAbility sa = AllZone.Stack.pop();
                            AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                        }
                    };
                    
                    if(card.getController().equals(Constant.Player.Computer)) {
                        AllZone.InputControl.setInput(new Input_PayManaCost_Ability(card + "\r\n",
                                ability.getManaCost(), Command.Blank, unpaidCommand));
                    } else {
                        if(ComputerUtil.canPayCost(ability)) ComputerUtil.playNoStack(ability);
                        else {
                            SpellAbility sa = AllZone.Stack.pop();
                            AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                        }
                    }
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    
                    //is spell?, did opponent play it?, is this a creature spell?
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && !sa.getSourceCard().getType().contains("Creature")
                            && CardFactoryUtil.isCounterable(sa.getSourceCard());
                }//canPlay()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        //*************** START *********** START **************************
        else if(cardName.equals("Mana Leak") || cardName.equals("Convolute") || cardName.equals("Daze")
                || cardName.equals("Force Spike") || cardName.equals("Runeboggle")
                || cardName.equals("Spell Snip") || cardName.equals("Mana Tithe")
                || cardName.equals("Miscalculation")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6139754377230333678L;
                
                @Override
                public void resolve() {
                    String manaCost = "1";
                    if(cardName.equals("Miscalculation")) manaCost = "2";
                    else if(cardName.equals("Mana Leak")) manaCost = "3";
                    else if(cardName.equals("Convolute")) manaCost = "4";
                    Ability ability = new Ability(card, manaCost) {
                        @Override
                        public void resolve() {
                            ;
                        }
                    };
                    
                    final Command unpaidCommand = new Command() {
                        private static final long serialVersionUID = 8094833091127334678L;
                        
                        public void execute() {
                            SpellAbility sa = AllZone.Stack.pop();
                            AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                        }
                    };
                    
                    if(card.getController().equals(Constant.Player.Computer)) {
                        AllZone.InputControl.setInput(new Input_PayManaCost_Ability(card + "\r\n",
                                ability.getManaCost(), Command.Blank, unpaidCommand));
                    } else {
                        if(ComputerUtil.canPayCost(ability)) ComputerUtil.playNoStack(ability);
                        else {
                            SpellAbility sa = AllZone.Stack.pop();
                            AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                        }
                    }
                    
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && CardFactoryUtil.isCounterable(sa.getSourceCard());
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            if(cardName.equals("Daze")) {
                spell.setDescription("Counter target spell unless its controller pays 1.");
                spell.setStackDescription(card.getName() + " - Counter target spell unless its controller pays 1.");
                final SpellAbility bounce = new Spell(card) {
                    private static final long serialVersionUID = -8310299673731730438L;
                    
                    @Override
                    public void resolve() {
                        SpellAbility sa = AllZone.Stack.pop();
                        AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                    }
                    
                    @Override
                    public boolean canPlay() {
                        if(AllZone.Stack.size() == 0) return false;
                        
                        //see if spell is on stack and that opponent played it
                        String opponent = AllZone.GameAction.getOpponent(card.getController());
                        SpellAbility sa = AllZone.Stack.peek();
                        
                        PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                        CardList list = new CardList(play.getCards());
                        list = list.getType("Island");
                        return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                                && CardFactoryUtil.isCounterable(sa.getSourceCard()) && list.size() >= 1;
                    }
                    
                    @Override
                    public boolean canPlayAI() {
                        return false;
                    }
                    
                };
                bounce.setDescription("You may return an Island you control to their owner's hand rather than pay Daze's mana cost.");
                bounce.setStackDescription(card.getName()
                        + " - Counter target spell unless its controller pays 1.");
                bounce.setManaCost("0");
                
                final Input bounceIslands = new Input() {
                    private static final long serialVersionUID = 7624182730685889456L;
                    int                       stop             = 1;
                    int                       count            = 0;
                    
                    @Override
                    public void showMessage() {
                        AllZone.Display.showMessage("Select an Island");
                        ButtonUtil.disableAll();
                    }
                    
                    @Override
                    public void selectButtonCancel() {
                        stop();
                    }
                    
                    @Override
                    public void selectCard(Card c, PlayerZone zone) {
                        if(c.getType().contains("Island") && zone.is(Constant.Zone.Play)) {
                            AllZone.GameAction.moveToHand(c);
                            
                            count++;
                            if(count == stop) {
                                AllZone.Stack.add(bounce);
                                stop();
                            }
                        }
                    }//selectCard()
                };
                
                bounce.setBeforePayMana(bounceIslands);
                card.addSpellAbility(bounce);
            }//if Daze
            else // This is Chris' Evil hack to get the Cycling cards to give us a choose window with text for the SpellAbility
            {
                spell.setDescription(card.getText());
                card.setText("");
            }
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Impulse")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6793636573741251978L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    CardList top = new CardList();
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    Card c;
                    int j = 4;
                    if(library.size() < 4) j = library.size();
                    for(int i = 0; i < j; i++) {
                        c = library.get(0);
                        library.remove(0);
                        top.add(c);
                    }
                    
                    if(top.size() >= 1) {
                        //let user get choice
                        Card chosen = AllZone.Display.getChoice("Choose a card to put into your hand",
                                top.toArray());
                        top.remove(chosen);
                        
                        //put card in hand
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                        hand.add(chosen);
                        
                        //add cards to bottom of library
                        for(int i = 0; i < top.size(); i++)
                            library.add(top.get(i));
                    }
                }//resolve()
            };//SpellAbility
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        

        //*************** START *********** START **************************
        else if(cardName.equals("Echoing Truth")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 563933533543239220L;
                
                @Override
                public boolean canPlayAI() {
                    CardList human = CardFactoryUtil.AI_getHumanCreature(card, true);
                    return 4 < AllZone.Phase.getTurn() && 0 < human.size();
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList human = CardFactoryUtil.AI_getHumanCreature(card, true);
                    setTargetCard(CardFactoryUtil.AI_getBestCreature(human));
                }
                
                @Override
                public void resolve() {
                    //if target card is not in play, just quit
                    if(!AllZone.GameAction.isCardInPlay(getTargetCard())
                            || !CardFactoryUtil.canTarget(card, getTargetCard())) return;
                    
                    //get all permanents
                    CardList all = new CardList();
                    all.addAll(AllZone.Human_Play.getCards());
                    all.addAll(AllZone.Computer_Play.getCards());
                    
                    CardList sameName = all.getName(getTargetCard().getName());
                    sameName = sameName.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card c)
                    	{
                    		return !c.isFaceDown();
                    	}
                    });
                    
                    if(!getTargetCard().isFaceDown()) {
                        //bounce all permanents with the same name
                        for(int i = 0; i < sameName.size(); i++) {
                            if(sameName.get(i).isToken()) AllZone.GameAction.removeFromGame(sameName.get(i));
                            else {
                                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, sameName.get(i).getOwner());
                                AllZone.GameAction.moveTo(hand, sameName.get(i));
                            }
                        }//for
                    }//if (!isFaceDown())
                    else {
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, getTargetCard().getOwner());
                        AllZone.GameAction.moveTo(hand, getTargetCard());
                    }
                }//resolve()
            };//SpellAbility
            Input target = new Input() {
                private static final long serialVersionUID = -3978705328511825933L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target non-land permanent for " + spell.getSourceCard());
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(!card.isLand() && zone.is(Constant.Zone.Play) && CardFactoryUtil.canTarget(spell, card)) {
                        spell.setTargetCard(card);
                        if (this.isFree())
                        {
                        	this.setFree(false);
                            AllZone.Stack.add(spell);
                            stop();
                        }
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//Input
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(target);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Opt")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6002051826637535590L;
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    //if top card of library is a land, put it on bottom of library
                    if(AllZone.Computer_Library.getCards().length != 0) {
                        Card top = AllZone.Computer_Library.get(0);
                        if(top.isLand()) {
                            AllZone.Computer_Library.remove(top);
                            AllZone.Computer_Library.add(top);
                        }
                    }
                    // AllZone.GameAction.drawCard(card.getController());
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    //see if any cards are in library
                    if(library.getCards().length != 0) {
                        Card top = library.get(0);
                        
                        Object o = top;
                        while(o instanceof Card)
                            o = AllZone.Display.getChoice("Do you want draw this card?", new Object[] {
                                    top, "Yes", "No"});
                        
                        if(o.toString().equals("No")) {
                            library.remove(top);
                            library.add(top);
                        }
                    }//if
                    // AllZone.GameAction.drawCard(card.getController());
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Worldly Tutor")) {
            SpellAbility spell = new Spell(card) {
                
				private static final long serialVersionUID = -2388471137292697028L;

				@Override
                public boolean canPlayAI() {
                    return 6 < AllZone.Phase.getTurn();
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    CardList creature = new CardList(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    if(creature.size() != 0) {
                        Card c = creature.get(0);
                        AllZone.GameAction.shuffle(card.getController());
                        
                        //move to top of library
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Library.add(c, 0);
                        
                        CardList list = new CardList();
                        list.add(c);
                        AllZone.Display.getChoiceOptional("Computer picked:", list.toArray());
                    }
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList(library.getCards());
                    list = list.getType("Creature");
                    
                    if(list.size() != 0) {
                        Object o = AllZone.Display.getChoiceOptional("Select a creature", list.toArray());
                        
                        AllZone.GameAction.shuffle(card.getController());
                        if(o != null) {
                            //put creature on top of library
                            library.remove(o);
                            library.add((Card) o, 0);
                        }
                    }//if
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
/* Converted to keyword        
        //*************** START *********** START **************************
        else if(cardName.equals("Enlightened Tutor")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2281623056004772379L;
                
                @Override
                public boolean canPlayAI() {
                    return 4 < AllZone.Phase.getTurn();
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    CardList list = new CardList(AllZone.Computer_Library.getCards());
                    CardList encharts = new CardList();
                    
                    for(int i = 0; i < list.size(); i++) {
                        if(list.get(i).getType().contains("Artifact")
                                || list.get(i).getType().contains("Enchantment")) encharts.add(list.get(i));
                    }
                    
                    if(encharts.size() != 0) {
                        //comp will just grab the first one it finds
                        Card c = encharts.get(0);
                        AllZone.GameAction.shuffle(card.getController());
                        

                        //move to top of library
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Library.add(c, 0);
                        
                        CardList l = new CardList();
                        l.add(c);
                        AllZone.Display.getChoiceOptional("Computer picked:", l.toArray());
                    }
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList(library.getCards());
                    CardList encharts = new CardList();
                    
                    for(int i = 0; i < list.size(); i++) {
                        if(list.get(i).getType().contains("Artifact")
                                || list.get(i).getType().contains("Enchantment")) encharts.add(list.get(i));
                    }
                    

                    if(encharts.size() != 0) {
                        Object o = AllZone.Display.getChoiceOptional("Select an artifact or enchantment",
                                encharts.toArray());
                        
                        AllZone.GameAction.shuffle(card.getController());
                        if(o != null) {
                            //put card on top of library
                            library.remove(o);
                            library.add((Card) o, 0);
                        }
                    }//if
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
 		*/

        /* Converted to keyword 
        //*************** START *********** START **************************
        else if(cardName.equals("Mystical Tutor")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2281623056004772379L;
                
                @Override
                public boolean canPlayAI() {
                    return 4 < AllZone.Phase.getTurn();
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    CardList list = new CardList(AllZone.Computer_Library.getCards());
                    CardList instantsAndSorceries = new CardList();
                    
                    for(int i = 0; i < list.size(); i++) {
                        if(list.get(i).getType().contains("Instant") || list.get(i).getType().contains("Sorcery")) instantsAndSorceries.add(list.get(i));
                    }
                    
                    if(instantsAndSorceries.size() != 0) {
                        //comp will just grab the first one it finds
                        Card c = instantsAndSorceries.get(0);
                        AllZone.GameAction.shuffle(card.getController());
                        

                        //move to top of library
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Library.add(c, 0);
                        
                        CardList l = new CardList();
                        l.add(c);
                        AllZone.Display.getChoiceOptional("Computer picked:", l.toArray());
                    }
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList(library.getCards());
                    CardList instantsAndSorceries = new CardList();
                    
                    for(int i = 0; i < list.size(); i++) {
                        if(list.get(i).getType().contains("Instant") || list.get(i).getType().contains("Sorcery")) instantsAndSorceries.add(list.get(i));
                    }
                    

                    if(instantsAndSorceries.size() != 0) {
                        Object o = AllZone.Display.getChoiceOptional("Select an instant or sorcery",
                                instantsAndSorceries.toArray());
                        
                        AllZone.GameAction.shuffle(card.getController());
                        if(o != null) {
                            //put card on top of library
                            library.remove(o);
                            library.add((Card) o, 0);
                        }
                    }//if
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        */
        
        //*************** START *********** START **************************
        else if(cardName.equals("Think Twice")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2571730013113893086L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.drawCard(card.getController());
                }//resolve()
            };
            card.clearSpellAbility();
            card.setFlashback(true);
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "2 U", "0"));
            
        }//*************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Elephant Ambush")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 1808366787563573082L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Elephant", "G 3 3 Elephant", card, "G", new String[] {
                            "Creature", "Elephant"}, 3, 3, new String[] {""});
                }//resolve()
            };
            
            spell.setDescription("Put a 3/3 green Elephant creature token into play.");
            spell.setStackDescription(card.getController()
                    + " puts a 3/3 green Elephant creature token into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "6 G G", "0"));
            
        }//*************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Beast Attack")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 381753184772980686L;
                
                @Override
                public void resolve() {
                    makeToken();
                }
                
                //for some reason, without this the AI can keep casting Beast Attack over and over.
                @Override
                public boolean canPlayAI() {
                    return !AllZone.GameAction.isCardInGrave(card);
                }
                
                public void makeToken() {
                    CardFactoryUtil.makeToken("Beast", "G 4 4 Beast", card, "G",
                            new String[] {"Creature", "Beast"}, 4, 4, new String[] {""});
                }//resolve()
            };
            
            spell.setDescription("Put a 4/4 green Beast creature token into play.");
            spell.setStackDescription(card.getController() + " put a 4/4 green Beast creature token into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "2 G G G", "0"));
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sprout")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 1299216756153970592L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Saproling", "G 1 1 Saproling", card, "G", new String[] {
                            "Creature", "Saproling"}, 1, 1, new String[] {""});
                }
            };
            
            spell.setDescription("Put a 1/1 green Saproling creature token into play.");
            spell.setStackDescription(card.getController()
                    + " put a 1/1 green Saproling creature token into play.");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Wrap in Vigor")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4235465815975050436L;
                
                @Override
                public boolean canPlayAI() {
                    return getAttacker() != null;
                }
                
                public Card getAttacker() {
                    //target creature that is going to attack
                    Combat c = ComputerUtil.getAttackers();
                    Card[] att = c.getAttackers();
                    if(att.length != 0) return att[0];
                    else return null;
                }//getAttacker()
                
                @Override
                public void resolve() {
                    final Card[] c = AllZone.getZone(Constant.Zone.Play, card.getController()).getCards();
                    
                    for(int i = 0; i < c.length; i++)
                        if(c[i].isCreature()) c[i].addShield();
                    
                    AllZone.EndOfTurn.addUntil(new Command() {
                        private static final long serialVersionUID = -3946800525315027053L;
                        
                        public void execute() {
                            for(int i = 0; i < c.length; i++)
                                c[i].resetShield();
                        }
                    });
                    
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Smother")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6479035316340603704L;
                
                @Override
                public boolean canPlayAI() {
                    CardList c = CardFactoryUtil.AI_getHumanCreature(true, 3, card, true);
                    CardListUtil.sortAttack(c);
                    CardListUtil.sortFlying(c);
                    
                    if(c.isEmpty()) return false;
                    else {
                        setTargetCard(c.get(0));
                        return true;
                    }
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c)
                            && CardUtil.getConvertedManaCost(card.getManaCost()) <= 3
                            && CardFactoryUtil.canTarget(card, getTargetCard())) AllZone.GameAction.destroyNoRegeneration(c);
                }//resolve()
            };//SpellAbility
            
            Input target = new Input() {
                private static final long serialVersionUID = 1877945605889747187L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target creature for " + card.getName()
                            + " - creature must have a converted manacost of 3 or less");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(!CardFactoryUtil.canTarget(spell, card)) {
                        AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                    }
                    if(card.isCreature() && zone.is(Constant.Zone.Play)
                            && CardUtil.getConvertedManaCost(card.getManaCost()) <= 3) {
                        spell.setTargetCard(card);
                        if(this.isFree()) 
                        {
                        	this.setFree(false);
                        	AllZone.Stack.add(spell);
                        	stop();
                    	} 
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//Input
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        

        
        //*************** START *********** START **************************
        else if(cardName.equals("Strangling Soot")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -3598479453933951865L;
                
                @Override
                public boolean canPlayAI() {
                    CardList c = CardFactoryUtil.AI_getHumanCreature(3, card, true);
                    CardListUtil.sortAttack(c);
                    CardListUtil.sortFlying(c);
                    
                    if(c.isEmpty()) return false;
                    else {
                        setTargetCard(c.get(0));
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
                        return AllZone.GameAction.isCardInZone(card, hand);
                    }
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c) && c.getNetDefense() <= 3
                            && CardFactoryUtil.canTarget(card, getTargetCard())) AllZone.GameAction.destroy(c);
                    
                }//resolve()
            };//SpellAbility
            
            final SpellAbility flashback = new Spell(card) {
                
                private static final long serialVersionUID = -4009531242109129036L;
                
                @Override
                public boolean canPlay() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    
                    return AllZone.GameAction.isCardInZone(card, grave);
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList c = CardFactoryUtil.AI_getHumanCreature(3, card, true);
                    CardListUtil.sortAttack(c);
                    CardListUtil.sortFlying(c);
                    
                    if(c.isEmpty()) return false;
                    else {
                        setTargetCard(c.get(0));
                        return true;
                    }
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    PlayerZone removed = AllZone.getZone(Constant.Zone.Removed_From_Play, card.getController());
                    
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c) && c.getNetDefense() <= 3
                            && CardFactoryUtil.canTarget(card, getTargetCard())) AllZone.GameAction.destroy(c);
                    
                    grave.remove(card);
                    removed.add(card);
                }//resolve()
            };//flashback
            
            Input targetFB = new Input() {
                
                private static final long serialVersionUID = -5469698194749752297L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target creature for " + card.getName()
                            + " - creature must have a toughness of 3 or less");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(!CardFactoryUtil.canTarget(flashback, card)) {
                        AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                    }
                    if(card.isCreature() && zone.is(Constant.Zone.Play) && card.getNetDefense() <= 3) {
                        flashback.setTargetCard(card);
                        stopSetNext(new Input_PayManaCost(flashback));
                    }
                }
            };//Input
            
            flashback.setFlashBackAbility(true);
            flashback.setManaCost("5 R");
            flashback.setBeforePayMana(targetFB);
            flashback.setDescription("Flashback: 5 R");
            
            Input target = new Input() {
                private static final long serialVersionUID = -198153850086215235L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target creature for " + card.getName()
                            + " - creature must have a toughness of 3 or less");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(!CardFactoryUtil.canTarget(spell, card)) {
                        AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                    }
                    if(card.isCreature() && zone.is(Constant.Zone.Play) && card.getNetDefense() <= 3) {
                    	spell.setTargetCard(card);
                    	if(this.isFree()) 
                        {
                        	this.setFree(false);
                        	AllZone.Stack.add(spell);
                        	stop();
                    	} 
                    	else 
                    		stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//Input
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(target);
            
            card.addSpellAbility(flashback);
            
            card.setFlashback(true);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Funeral Charm")) {
            
            //discard
            final SpellAbility spell_one = new Spell(card) {
                private static final long serialVersionUID = 8273875515630095127L;
                
                @Override
                public boolean canPlayAI() {

                    setTargetPlayer(Constant.Player.Human);
                    PlayerZone humanHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human);
                    
                    return (humanHand.size() >= 1);
                }
                
                @Override
                public void resolve() {
                    if (Constant.Player.Computer.equals(getTargetPlayer())) AllZone.GameAction.discardRandom(getTargetPlayer(), this);
                    else AllZone.InputControl.setInput(CardFactoryUtil.input_discard(this));
                }//resolve()
            };//SpellAbility
            
            spell_one.setDescription("Target player discards a card.");
            spell_one.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell_one));
  
            //creature gets +2/-1
            final SpellAbility spell_two = new Spell(card) {
                private static final long serialVersionUID = -4554812851052322555L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    final Card c = getTargetCard();
                    
                    if (AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        c.addTempAttackBoost(2);
                        c.addTempDefenseBoost(-1);
                        
                        Command until = new Command() {
                            private static final long serialVersionUID = 4674846621452044251L;
                            
                            public void execute() {
                                c.addTempAttackBoost(-2);
                                c.addTempDefenseBoost(1);
                            }
                        };//Command
                        AllZone.EndOfTurn.addUntil(until);
                    }//if card in play?
                }//resolve()
            };//SpellAbility
            spell_two.setDescription("Target creature gets +2/-1 until end of turn.");
            spell_two.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell_two));
            
            //creature gets swampwalk
            final SpellAbility spell_three = new Spell(card) {
                private static final long serialVersionUID = -8455677074284271852L;

                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    final Card c = getTargetCard();
                    
                    if (AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c) && !c.getKeyword().contains("Swampwalk")) {
                        c.addExtrinsicKeyword("Swampwalk");
                        
                        Command until = new Command() {
                            private static final long serialVersionUID = 1452395016805444249L;

                            public void execute() {
                                if (AllZone.GameAction.isCardInPlay(c)) {
                                    c.removeExtrinsicKeyword("Swampwalk");
                                }
                            }
                        };//Command
                        AllZone.EndOfTurn.addUntil(until);
                    }//if card in play?
                }//resolve()
            };//SpellAbility
            spell_three.setDescription("Target creature gains swampwalk until end of turn.");
            spell_three.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell_three));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell_one);
            card.addSpellAbility(spell_two);
            card.addSpellAbility(spell_three);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if( cardName.equals("Reclaim")) {
            final SpellAbility spell = new Spell(card) {
                
				private static final long serialVersionUID = -7352293983206222113L;

				@Override
                public void resolve() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    
                    if(AllZone.GameAction.isCardInZone(getTargetCard(), graveyard)) {
                        graveyard.remove(getTargetCard());
                        AllZone.GameAction.moveToTopOfLibrary(getTargetCard());
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    return graveyard.getCards().length != 0 && super.canPlay();
                }
            };
            Input runtime = new Input() {
				private static final long serialVersionUID = -1438178075940689742L;

				@Override
                public void showMessage() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    Object o = AllZone.Display.getChoiceOptional("Select target card", graveyard.getCards());
                    if(o == null) stop();
                    else {
                    	String location = "top of owner's library";
                    	
                        spell.setStackDescription("Return " + o + " to " + location);
                        spell.setTargetCard((Card) o);
                        if(this.isFree()) 
                        {
                        	// WARNING: Read this before copying!
                        	// When we have an 'if (this.isFree())' in most input objects,
                        	// it's inside 'selectCard' not 'showMessage', and the usual 
                        	// order is 
                        	//   AllZone.Stack.add(spell);
                        	//   stop();
                        	// Here, we had to reverse the order of those two lines, or 
                        	// else the dialog for Regrowth would get put up twice 
                        	// when the card was played from Cascade. I think this 
                        	// has to do with when showMessage() is called versus 
                        	// selectCard().
                        	// This appears to be the only place we use this pattern. Be
                        	// careful when copying this code, and test your card with
                        	// Cascade or Isochron Scepter.
                        	this.setFree(false);
                        	stop();
                        	AllZone.Stack.add(spell);
                    	} 
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }//showMessage()
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetType("All", AllZone.Computer_Graveyard));
            spell.setBeforePayMana(runtime);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************       
        
        //*************** START *********** START **************************
        else if(cardName.equals("Eladamri's Call")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6495398165357932918L;
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void humanResolve() {
                    CardList creatures = new CardList(AllZone.Human_Library.getCards());
                    creatures = creatures.getType("Creature");
                    
                    Object check = AllZone.Display.getChoiceOptional("Select creature", creatures.toArray());
                    if(check != null) {
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                        AllZone.GameAction.moveTo(hand, (Card) check);
                    }
                    AllZone.GameAction.shuffle(Constant.Player.Human);
                }
                
                public void computerResolve() {
                    Card[] library = AllZone.Computer_Library.getCards();
                    CardList list = new CardList(library);
                    list = list.getType("Creature");
                    
                    if(list.size() > 0) {
                        //pick best creature
                        Card c = CardFactoryUtil.AI_getBestCreature(list);
                        if(c == null) c = list.get(0);
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Hand.add(c);
                        CardList cl = new CardList();
                        cl.add(c);
                        AllZone.Display.getChoiceOptional("Computer picked:", cl.toArray());
                    }
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    return library.getCards().length != 0;
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList creature = new CardList();
                    creature.addAll(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    return creature.size() != 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Momentous Fall")) {
            final SpellAbility spell = new Spell(card) {
                
    			private static final long serialVersionUID = -56339412409L;


    			@Override
                public void resolve() {
    				Card Sacrificed = getTargetCard();
    				if(Sacrificed != null) {
    					for(int i = 0; i < Sacrificed.getNetAttack(); i++) {
    						AllZone.GameAction.drawCard(card.getController());
    					}
                        PlayerLife life = AllZone.GameAction.getPlayerLife(card.getController());
                        life.addLife(Sacrificed.getNetDefense());
    				}
                  }
                
                
                @Override
                public boolean canPlay() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList Creatures = new CardList(play.getCards());
                    Creatures = Creatures.getType("Creature");                   
                    return Creatures.size() > 0 && super.canPlay();                   
                }
                @Override
                public boolean canPlayAI() {                  
                    return false;                   
                }         
            };
            Input target = new Input() {
                
                private static final long serialVersionUID = 42466124531655L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a creature to sacrifice");
                    ButtonUtil.enableOnlyCancel();
                }
                
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    PlayerZone Play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    if(AllZone.GameAction.isCardInZone(c, Play) && c.isCreature() == true) {
                    	spell.setTargetCard(c);
                    	AllZone.GameAction.sacrifice(c);
                        AllZone.Stack.add(spell);
                        stopSetNext(new ComputerAI_StackNotEmpty());
                    }
                }//selectCard()
            };//Input
            card.clearSpellAbility();
            card.addSpellAbility(spell);        
            spell.setAfterPayMana(target);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Overwhelming Intellect")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -8825219868732813877L;
                
                @Override
                public void resolve() {
                    SpellAbility sa = AllZone.Stack.pop();
                    AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                    
                    int convertedManaCost = CardUtil.getConvertedManaCost(sa.getSourceCard().getManaCost());
                    for(int i = 0; i < convertedManaCost; i++) {
                        AllZone.GameAction.drawCard(card.getController());
                    }
                    
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && sa.getSourceCard().getType().contains("Creature")
                            && CardFactoryUtil.isCounterable(sa.getSourceCard());
                    

                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Banishing Knack")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6518824567946786581L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    final Card creature = getTargetCard();
                    final Ability_Tap tBanish = new Ability_Tap(creature) {
                        private static final long serialVersionUID = -1008113001678623984L;
                        
                        @Override
                        public boolean canPlayAI() {
                            return false;
                        }
                        
                        @Override
                        public void resolve() {
                            setStackDescription(creature + " - Return" + getTargetCard() + "to its owner's hand");
                            final Card[] target = new Card[1];
                            target[0] = getTargetCard();
                            PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, target[0].getOwner());
                            
                            if(AllZone.GameAction.isCardInPlay(target[0])
                                    && CardFactoryUtil.canTarget(creature, target[0])) {
                                AllZone.GameAction.moveTo(hand, target[0]);
                            }
                        }//resolve()
                    };//tBanish;
                    tBanish.setDescription("T: Return target nonland permanent to its owner's hand.");
                    creature.addSpellAbility(tBanish);
                    CardList all = new CardList();
                    all.addAll(AllZone.Human_Play.getCards());
                    all.addAll(AllZone.Computer_Play.getCards());
                    all = all.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (!c.isLand() && CardFactoryUtil.canTarget(creature, c));
                        }
                    });
                    
                    tBanish.setBeforePayMana(CardFactoryUtil.input_targetSpecific(tBanish, all,
                            "Return target nonland permanent to its owner's hand.", true, false));
                    AllZone.EndOfTurn.addUntil(new Command() {
                        private static final long serialVersionUID = -7819140065166374666L;
                        
                        public void execute() {
                            creature.removeSpellAbility(tBanish);
                        }
                    });
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setDescription("Until end of turn, target creature gains \"T: Return target nonland permanent to its owner's hand.\"");
            spell.setStackDescription("Target creature gains \"T: Return target nonland permanent to its owner's hand.\"");
        }//*************** END ************ END **************************
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Vampiric Tutor")) {
            SpellAbility spell = new Spell(card) {
                

				private static final long serialVersionUID = 8792913782443246572L;

				@Override
                public boolean canPlayAI() {
                    PlayerLife compLife = AllZone.GameAction.getPlayerLife("Computer");
                    int life = compLife.getLife();
                    if(4 < AllZone.Phase.getTurn() && AllZone.Computer_Library.size() > 0 && life >= 4) return true;
                    else return false;
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    //TODO: somehow select a good non-creature card for AI
                    CardList creature = new CardList(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    if(creature.size() != 0) {
                        Card c = CardFactoryUtil.AI_getBestCreature(creature);
                        
                        if(c == null) {
                            creature.shuffle();
                            c = creature.get(0);
                        }
                        
                        AllZone.GameAction.shuffle(card.getController());
                        
                        //move to top of library
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Library.add(c, 0);
                        
                        //lose 2 life
                        String player = Constant.Player.Computer;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(2,card);
                    }
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList(library.getCards());
                    
                    if(list.size() != 0) {
                        Object o = AllZone.Display.getChoiceOptional("Select a card", list.toArray());
                        
                        AllZone.GameAction.shuffle(card.getController());
                        if(o != null) {
                            //put card on top of library
                            library.remove(o);
                            library.add((Card) o, 0);
                        }
                        //lose 2 life
                        String player = Constant.Player.Human;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(2,card);
                    }//if
                    

                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Intuition")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8282597086298330698L;
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void humanResolve() {
                    CardList libraryList = new CardList(AllZone.Human_Library.getCards());
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList selectedCards = new CardList();
                    
                    Object o = AllZone.Display.getChoiceOptional("Select first card", libraryList.toArray());
                    if(o != null) {
                        Card c1 = (Card) o;
                        libraryList.remove(c1);
                        selectedCards.add(c1);
                    } else {
                        return;
                    }
                    o = AllZone.Display.getChoiceOptional("Select second card", libraryList.toArray());
                    if(o != null) {
                        Card c2 = (Card) o;
                        libraryList.remove(c2);
                        selectedCards.add(c2);
                    } else {
                        return;
                    }
                    o = AllZone.Display.getChoiceOptional("Select third card", libraryList.toArray());
                    if(o != null) {
                        Card c3 = (Card) o;
                        libraryList.remove(c3);
                        selectedCards.add(c3);
                    } else {
                        return;
                    }
                    
                    Card choice = selectedCards.get(MyRandom.random.nextInt(2)); //comp randomly selects one of the three cards
                    
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    library.remove(choice);
                    hand.add(choice);
                    
                    selectedCards.remove(choice);
                    Card toGrave1 = selectedCards.get(0);
                    Card toGrave2 = selectedCards.get(1);
                    library.remove(toGrave1);
                    library.remove(toGrave2);
                    selectedCards.remove(toGrave2);
                    selectedCards.remove(toGrave2);
                    
                    grave.add(toGrave1);
                    grave.add(toGrave2);
                    
                    AllZone.GameAction.shuffle(Constant.Player.Human);
                }
                
                public void computerResolve() {
                    Card[] library = AllZone.Computer_Library.getCards();
                    CardList list = new CardList(library);
                    CardList selectedCards = new CardList();
                    
                    //pick best creature
                    Card c = CardFactoryUtil.AI_getBestCreature(list);
                    if(c == null) {
                        c = library[0];
                    }
                    list.remove(c);
                    selectedCards.add(c);
                    
                    c = CardFactoryUtil.AI_getBestCreature(list);
                    if(c == null) {
                        c = library[0];
                    }
                    list.remove(c);
                    selectedCards.add(c);
                    
                    c = CardFactoryUtil.AI_getBestCreature(list);
                    if(c == null) {
                        c = library[0];
                    }
                    list.remove(c);
                    selectedCards.add(c);
                    
                    Object o = AllZone.Display.getChoiceOptional("Select card to give to computer",
                            selectedCards.toArray());
                    
                    Card choice = (Card) o;
                    
                    selectedCards.remove(choice);
                    AllZone.Computer_Library.remove(choice);
                    AllZone.Computer_Hand.add(choice);
                    
                    AllZone.Computer_Library.remove(selectedCards.get(0));
                    AllZone.Computer_Library.remove(selectedCards.get(1));
                    
                    AllZone.Computer_Graveyard.add(selectedCards.get(0));
                    AllZone.Computer_Graveyard.add(selectedCards.get(1));
                    
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    return library.getCards().length >= 3;
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList creature = new CardList();
                    creature.addAll(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    return creature.size() != 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("High Tide")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4997834721261916L;
                
                @Override
                public boolean canPlayAI() {                   
                    return false;
                }//canPlay()
                
                @Override
                public void resolve() {
                	Phase.HighTideCount = Phase.HighTideCount + 1;
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        


        //*************** START *********** START **************************
        else if(cardName.equals("Tithe")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1504792204526793942L;
                
                public boolean oppMoreLand() {
                    String oppPlayer = AllZone.GameAction.getOpponent(card.getController());
                    
                    PlayerZone selfZone = AllZone.getZone(Constant.Zone.Play, card.getController());
                    PlayerZone oppZone = AllZone.getZone(Constant.Zone.Play, oppPlayer);
                    
                    CardList self = new CardList(selfZone.getCards());
                    CardList opp = new CardList(oppZone.getCards());
                    
                    self = self.getType("Land");
                    opp = opp.getType("Land");
                    
                    return (self.size() < opp.size()) && super.canPlay();
                }//oppoMoreLand()
                
                @Override
                public void resolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    
                    CardList plains = new CardList(library.getCards());
                    plains = plains.getType("Plains");
                    
                    if(0 < plains.size()) AllZone.GameAction.moveTo(hand, plains.get(0));
                    
                    if(oppMoreLand() && 1 < plains.size()) AllZone.GameAction.moveTo(hand, plains.get(1));
                    
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Nameless Inversion")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 5479536291205544905L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = CardFactoryUtil.AI_getHumanCreature(3, card, true);
                    CardListUtil.sortFlying(list);
                    
                    for(int i = 0; i < list.size(); i++)
                        if(2 <= list.get(i).getNetAttack()) {
                            setTargetCard(list.get(i));
                            return true;
                        }
                    return false;
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    final Card[] target = new Card[1];
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = -1954104042512587145L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(target[0])) {
                                target[0].addTempAttackBoost(-3);
                                target[0].addTempDefenseBoost(3);
                            }
                        }
                    };
                    
                    target[0] = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0])) {
                        target[0].addTempAttackBoost(3);
                        target[0].addTempDefenseBoost(-3);
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }//resolve()
            };
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Primal Boost")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2449600319884238808L;
                
                @Override
                public boolean canPlayAI() {
                    return getAttacker() != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    setTargetCard(getAttacker());
                }
                
                public Card getAttacker() {
                    //target creature that is going to attack
                    Combat c = ComputerUtil.getAttackers();
                    
                    CardList list = new CardList(c.getAttackers());
                    CardListUtil.sortFlying(list);
                    
                    Card[] att = list.toArray();
                    if(att.length != 0) return att[0];
                    else return null;
                }//getAttacker()
                
                @Override
                public void resolve() {
                    final Card[] target = new Card[1];
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = 3753684523153747308L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(target[0])) {
                                target[0].addTempAttackBoost(-4);
                                target[0].addTempDefenseBoost(-4);
                            }
                        }
                    };
                    
                    target[0] = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0])) {
                        target[0].addTempAttackBoost(4);
                        target[0].addTempDefenseBoost(4);
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }//resolve()
            };
            spell.setDescription("\r\nTarget creature gets +4/+4 until end of turn.");
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            //card.addSpellAbility(CardFactoryUtil.ability_cycle(card, "2 G"));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Animate Land")) {
            final Card[] target = new Card[1];
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = -3359299797188942353L;
                
                public void execute() {
                    if(AllZone.GameAction.isCardInPlay(target[0])) {
                        target[0].removeType("Creature");
                    }
                }
            };
            
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4890851927124377327L;
                
                @Override
                public void resolve() {
                    target[0] = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0])) {
                        target[0].addType("Creature");
                        target[0].setBaseAttack(3);
                        target[0].setBaseDefense(3);
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                    /* all this doesnt work, computer will not attack with the animated land

                    //does the computer have any land in play?
                    CardList land = new CardList(AllZone.Computer_Play.getCards());
                    land = land.getType("Land");
                    land = land.filter(new CardListFilter()
                    {
                      public boolean addCard(Card c)
                      {
                              //checks for summoning sickness, and is not tapped
                        return CombatUtil.canAttack(c);
                      }
                    });
                    return land.size() > 1 && CardFactoryUtil.AI_isMainPhase();
                    */
                }
            };//SpellAbility
//      spell.setChooseTargetAI(CardFactoryUtil.AI_targetType("Land", AllZone.Computer_Play));
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "Land"));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Seething Song")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 113811381138L;
                
                @Override
                public void resolve() {
                    //CardList list = new CardList(AllZone.getZone(Constant.Zone.Play, Constant.Player.Human).getCards());
                    //list = list.getName("Mana Pool");
                    Card mp = AllZone.ManaPool;//list.getCard(0);
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:R");
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            
            spell.setStackDescription("Adds R R R R R to your mana pool");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            return card;
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Dark Ritual")) {
            final SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = -8579887529151755266L;
                
                @Override
                public void resolve() {
                    /*CardList list = new CardList(AllZone.getZone(Constant.Zone.Play, Constant.Player.Human).getCards());
                    list = list.getName("Mana Pool");*/
                    Card mp = AllZone.ManaPool;//list.getCard(0);
                    mp.addExtrinsicKeyword("ManaPool:B");
                    mp.addExtrinsicKeyword("ManaPool:B");
                    mp.addExtrinsicKeyword("ManaPool:B");
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" adds B B B to your mana pool");
            spell.setStackDescription(sb.toString());
            
            // spell.setStackDescription(cardName + " adds B B B to your mana pool");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            return card;
        }//*************** END ************ END **************************


        
        //*************** START *********** START **************************
        else if (cardName.equals("Pyretic Ritual")) {
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -5473428583650237774L;

				@Override
                public void resolve() {
                    /*CardList list = new CardList(AllZone.getZone(Constant.Zone.Play, Constant.Player.Human).getCards());
                    list = list.getName("Mana Pool");*/
                    Card mp = AllZone.ManaPool;//list.getCard(0);
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:R");
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" adds R R R to your mana pool");
            spell.setStackDescription(sb.toString());
            
            // spell.setStackDescription(cardName + " adds R R R to your mana pool");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            return card;
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Diabolic Edict") ) {
            final SpellAbility spell = new Spell(card) {
              
				private static final long serialVersionUID = 1593405082929818055L;

				@Override
                public void resolve() {
                    AllZone.GameAction.sacrificeCreature(getTargetPlayer(), this);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                    CardList hList = new CardList(hPlay.getCards());
                    hList = hList.getType("Creature");
                    return hList.size() > 0;
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
        }//*************** END ************ END **************************



        //*************** START *********** START **************************
        else if(cardName.equals("Path to Exile")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4752934806606319269L;
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        String player = getTargetCard().getController();
                        PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                        
                        //remove card from play
                        AllZone.GameAction.removeFromGame(getTargetCard());
                        
                        //Retrieve basic land
                        CardList lands = new CardList(lib.getCards());
                        lands = lands.getType("Basic");
                        
                        if(player.equals("Human") && lands.size() > 0) {
                            String[] choices = {"Yes", "No"};
                            Object choice = AllZone.Display.getChoice("Search for Basic Land?", choices);
                            if(choice.equals("Yes")) {
                                Object o = AllZone.Display.getChoiceOptional(
                                        "Pick a basic land card to put into play", lands.toArray());
                                if(o != null) {
                                    Card card = (Card) o;
                                    lib.remove(card);
                                    AllZone.Human_Play.add(card);
                                    card.tap();
                                    lands.remove(card);
                                    AllZone.GameAction.shuffle(player);
                                }
                            }// if choice yes
                        } // player equals human
                        else if(player.equals("Computer") && lands.size() > 0) {
                            Card card = lands.get(0);
                            lib.remove(card);
                            // hand.add(card);
                            AllZone.Computer_Play.add(card);
                            card.tap();
                            lands.remove(card);
                            AllZone.GameAction.shuffle(player);
                        }
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    CardList creature = new CardList(AllZone.Human_Play.getCards());
                    creature = creature.getType("Creature");
                    creature = creature.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    return creature.size() != 0 && (AllZone.Phase.getTurn() > 4);
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList play = new CardList(AllZone.Human_Play.getCards());
                    Card target = CardFactoryUtil.AI_getBestCreature(play, card);
                    setTargetCard(target);
                }
            };
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Gush")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8881817765689776033L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.drawCard(card.getController());
                    AllZone.GameAction.drawCard(card.getController());
                }
            };
            spell.setDescription("Draw two cards.");
            spell.setStackDescription(card.getName() + " - Draw two cards.");
            
            final SpellAbility bounce = new Spell(card) {
                private static final long serialVersionUID = 1950742710354343569L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.drawCard(card.getController());
                    AllZone.GameAction.drawCard(card.getController());
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Island");
                    return list.size() >= 2;
                }
                
            };
            bounce.setDescription("You may return two Islands you control to their owner's hand rather than pay Gush's mana cost.");
            bounce.setStackDescription(card.getName() + " - Draw two cards.");
            bounce.setManaCost("0");
            
            final Input bounceIslands = new Input() {
                private static final long serialVersionUID = 3124427514142382129L;
                int                       stop             = 2;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select an Island");
                    ButtonUtil.disableAll();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(c.getType().contains("Island") && zone.is(Constant.Zone.Play)) {
                        AllZone.GameAction.moveToHand(c);
                        
                        count++;
                        if(count == stop) {
                            AllZone.Stack.add(bounce);
                            stop();
                        }
                    }
                }//selectCard()
            };
            
            bounce.setBeforePayMana(bounceIslands);
            
            Command bounceIslandsAI = new Command() {
                private static final long serialVersionUID = 235908265780575226L;
                
                public void execute() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Island");
                    //TODO: sort by tapped
                    
                    for(int i = 0; i < 2; i++) {
                        AllZone.GameAction.moveToHand(list.get(i));
                    }
                }
            };
            
            bounce.setBeforePayManaAI(bounceIslandsAI);
            
            card.clearSpellAbility();
            card.addSpellAbility(bounce);
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Thwart")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6549506712141125977L;
                
                @Override
                public void resolve() {
                    SpellAbility sa = AllZone.Stack.pop();
                    AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && CardFactoryUtil.isCounterable(sa.getSourceCard());
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            spell.setDescription("Counter target spell.");
            spell.setStackDescription(card.getName() + " - Counter target spell.");
            
            final SpellAbility bounce = new Spell(card) {
                private static final long serialVersionUID = -8310299673731730438L;
                
                @Override
                public void resolve() {
                    SpellAbility sa = AllZone.Stack.pop();
                    AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Island");
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && CardFactoryUtil.isCounterable(sa.getSourceCard()) && list.size() >= 3;
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
            };
            bounce.setDescription("You may return three Islands you control to their owner's hand rather than pay Thwart's mana cost.");
            bounce.setStackDescription(card.getName() + " - Counter target spell.");
            bounce.setManaCost("0");
            
            final Input bounceIslands = new Input() {
                private static final long serialVersionUID = 3124427514142382129L;
                int                       stop             = 3;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select an Island");
                    ButtonUtil.disableAll();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(c.getType().contains("Island") && zone.is(Constant.Zone.Play)) {
                        AllZone.GameAction.moveToHand(c);
                        
                        count++;
                        if(count == stop) {
                            AllZone.Stack.add(bounce);
                            stop();
                        }
                    }
                }//selectCard()
            };
            
            bounce.setBeforePayMana(bounceIslands);
            
            Command bounceIslandsAI = new Command() {
                private static final long serialVersionUID = 8250154784542733353L;
                
                public void execute() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Island");
                    //TODO: sort by tapped
                    
                    for(int i = 0; i < 3; i++) {
                        AllZone.GameAction.moveToHand(list.get(i));
                    }
                }
            };
            
            bounce.setBeforePayManaAI(bounceIslandsAI);
            
            card.clearSpellAbility();
            card.addSpellAbility(bounce);
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Force of Will")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 7960371805654673281L;
                
                @Override
                public void resolve() {
                    SpellAbility sa = AllZone.Stack.pop();
                    AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && CardFactoryUtil.isCounterable(sa.getSourceCard());
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            spell.setDescription("Counter target spell.");
            spell.setStackDescription(card.getName() + " - Counter target spell.");
            
            final SpellAbility alt = new Spell(card) {
                private static final long serialVersionUID = -8643870743780757816L;
                
                @Override
                public void resolve() {
                    SpellAbility sa = AllZone.Stack.pop();
                    AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() == 0) return false;
                    
                    //see if spell is on stack and that opponent played it
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    SpellAbility sa = AllZone.Stack.peek();
                    
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    CardList list = new CardList(hand.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return CardUtil.getColors(c).contains(Constant.Color.Blue) && !c.equals(card);
                        }
                    });
                    return sa.isSpell() && opponent.equals(sa.getSourceCard().getController())
                            && CardFactoryUtil.isCounterable(sa.getSourceCard()) && list.size() >= 1;
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
            };
            alt.setDescription("You may pay 1 life and exile a blue card from your hand rather than pay Force of Will's mana cost.");
            alt.setStackDescription(card.getName() + " - Counter target spell.");
            alt.setManaCost("0");
            
            final Input exileBlue = new Input() {
                private static final long serialVersionUID = 8692998689009712987L;
                int                       stop             = 1;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a blue card");
                    ButtonUtil.disableAll();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(CardUtil.getColors(c).contains(Constant.Color.Blue) && zone.is(Constant.Zone.Hand)
                            && !c.equals(card)) {
                        AllZone.GameAction.removeFromGame(c);
                        String player = card.getController();
                        AllZone.GameAction.getPlayerLife(player).subtractLife(1,card);
                        
                        count++;
                        if(count == stop) {
                            AllZone.Stack.add(alt);
                            stop();
                        }
                    }
                }//selectCard()
            };
            

            alt.setBeforePayMana(exileBlue);
            
            /*
            Command bounceIslandsAI = new Command()
            {
            private static final long serialVersionUID = -8745630329512914365L;

            public void execute()
              {
            	  PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
            	  CardList list = new CardList(play.getCards());
            	  list = list.getType("Island");
            	  //TODO: sort by tapped
            	  
            	  for (int i=0;i<3;i++)
            	  {
            		  AllZone.GameAction.moveToHand(list.get(i));
            	  }  
              }
            };
            
            alt.setBeforePayManaAI(bounceIslandsAI);
            */

            card.clearSpellAbility();
            card.addSpellAbility(alt);
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Join the Ranks")) {
            SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 2700238195526474372L;

				@Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Soldier Ally", "W 1 1 Soldier Ally", card, "W",
                            new String[] {"Creature", "Soldier", "Ally"}, 1, 1, new String[] {""});
                    CardFactoryUtil.makeToken("Soldier Ally", "W 1 1 Soldier Ally", card, "W",
                            new String[] {"Creature", "Soldier", "Ally"}, 1, 1, new String[] {""});
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
      
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Squall Line"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
			private static final long serialVersionUID = 8031146002062605694L;
			public void resolve()
      		{
  				int damage = card.getXManaCostPaid();
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && c.getKeyword().contains("Flying") &&
                  			   CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.GameAction.addDamage(Constant.Player.Human, card, damage);
                  AllZone.GameAction.addDamage(Constant.Player.Computer, card, damage);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 1;
  				
  				if (AllZone.Human_Life.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && c.getKeyword().contains("Flying") &&
  							   CardFactoryUtil.canDamage(card, c) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList humanFliers = new CardList(AllZone.Human_Play.getCards());
  			    humanFliers = humanFliers.filter(filter);
  			    
  			    CardList compFliers = new CardList(AllZone.Computer_Play.getCards());
  			    compFliers = compFliers.filter(filter);
  			    
  			    return humanFliers.size() > (compFliers.size() + 2) && AllZone.Computer_Life.getLife() > maxX + 3;
  			}
      	  };
      	  spell.setDescription(cardName + " deals X damage to each creature with flying and each player.");
      	  spell.setStackDescription(card + " - deals X damage to each creature with flying and each player.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Fault Line"))
        {
      	  final String[] keyword = new String[1];
      	  
      	  keyword[0] = "Flying";

      	  final SpellAbility spell = new Spell(card)
      	  {
			private static final long serialVersionUID = -1887664058112475665L;
			public void resolve()
      		{
  				int damage = card.getXManaCostPaid();
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && !c.getKeyword().contains(keyword[0]) &&
                  			   CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.GameAction.addDamage(Constant.Player.Human, card, damage);
                  AllZone.GameAction.addDamage(Constant.Player.Computer, card, damage);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - CardUtil.getConvertedManaCost(card);
  				
  				if (AllZone.Human_Life.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && !c.getKeyword().contains(keyword) &&
  							   CardFactoryUtil.canDamage(card, c) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList human = new CardList(AllZone.Human_Play.getCards());
  			    human = human.filter(filter);
  			    
  			    CardList comp = new CardList(AllZone.Computer_Play.getCards());
  			    comp = comp.filter(filter);
  			    
  			    return human.size() > (comp.size() + 2) && AllZone.Computer_Life.getLife() > maxX + 3;
  			}
      	  };
      	  spell.setDescription(cardName + " deals X damage to each creature without "+ keyword[0]+" and each player.");
      	  spell.setStackDescription(card + " - deals X damage to each creature without " +keyword[0] +" and each player.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
      
        
        //*************** START *********** START **************************
        else if(cardName.equals("Stroke of Genius"))
        {
      	  final SpellAbility spell = new Spell(card){
  			private static final long serialVersionUID = -7141472916367953810L;

  			public void resolve()
      		  {
      			  String player = getTargetPlayer();
      			  for(int i=0;i<card.getXManaCostPaid();i++)
      			  {
      				  AllZone.GameAction.drawCard(player);
      			  }
      			  card.setXManaCostPaid(0);
      		  }
      		  
      		  public boolean canPlayAI()
      		  {
      			  final int maxX = ComputerUtil.getAvailableMana().size() - 1;
      			  return maxX > 3 && AllZone.Computer_Hand.size() <= 3;
      		  }
      	  };
      	  spell.setDescription("Target player draws X cards.");
      	  spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
      	  spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if (cardName.equals("Windstorm"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
  			private static final long serialVersionUID = 6024081054401784073L;
  			public void resolve()
      		{
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && c.getKeyword().contains("Flying") &&
                  			   CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 1;
  								
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && c.getKeyword().contains("Flying") &&
  							   CardFactoryUtil.canDamage(card, c) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList humanFliers = new CardList(AllZone.Human_Play.getCards());
  			    humanFliers = humanFliers.filter(filter);
  			    
  			    CardList compFliers = new CardList(AllZone.Computer_Play.getCards());
  			    compFliers = compFliers.filter(filter);
  			    
  			    return humanFliers.size() > (compFliers.size() + 2);
  			}
      	  };
      	  spell.setDescription("Windstorm deals X damage to each creature with flying.");
      	  spell.setStackDescription("Windstorm - deals X damage to each creature with flying.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
          
        
        
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Echoing Courage"))
        {
          final SpellAbility spell = new Spell(card)
          {
  		  private static final long serialVersionUID = -8649611733196156346L;

      	  public boolean canPlayAI()
            {
              CardList c = getCreature();
              if(c.isEmpty())
                return false;
              else
              {
                setTargetCard(c.get(0));
                return true;
              }
            }//canPlayAI()
            CardList getCreature()
            {
              CardList out = new CardList();
              CardList list = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
              list.shuffle();

              for(int i = 0; i < list.size(); i++)
                if((list.get(i).getNetAttack() >= 2) && (list.get(i).getNetDefense() <= 2))
                  out.add(list.get(i));

              //in case human player only has a few creatures in play, target anything
              if(out.isEmpty() &&
                  0 < CardFactoryUtil.AI_getHumanCreature(2, card, true).size() &&
                 3 > CardFactoryUtil.AI_getHumanCreature(card, true).size())
              {
                out.addAll(CardFactoryUtil.AI_getHumanCreature(2, card, true).toArray());
                CardListUtil.sortFlying(out);
              }
              return out;
            }//getCreature()


            public void resolve()
            {
              if(AllZone.GameAction.isCardInPlay(getTargetCard()) && CardFactoryUtil.canTarget(card, getTargetCard()) )
              {
                final Card c = getTargetCard();
               
                c.addTempAttackBoost(2);
                 c.addTempDefenseBoost(2);

                 AllZone.EndOfTurn.addUntil(new Command()
                 {
                private static final long serialVersionUID = 1327455269456577020L;

                public void execute()
                    {
                       c.addTempAttackBoost(-2);
                       c.addTempDefenseBoost(-2);
                    }
                 });

                //get all creatures
                CardList list = new CardList();
                list.addAll(AllZone.Human_Play.getCards());
                list.addAll(AllZone.Computer_Play.getCards());

                list = list.getName(getTargetCard().getName());
                list.remove(getTargetCard());
                 
                if (!getTargetCard().isFaceDown())
                   for(int i = 0; i < list.size(); i++)
                   {
                      final Card crd = list.get(i);
                      
                      crd.addTempAttackBoost(2);
                      crd.addTempDefenseBoost(2);
                      
                      AllZone.EndOfTurn.addUntil(new Command()
                        {
                      private static final long serialVersionUID = 5151337777143949221L;

                      public void execute()
                           {
                              crd.addTempAttackBoost(-2);
                              crd.addTempDefenseBoost(-2);
                           }
                        });
                      //list.get(i).addDamage(2);
                   }
                    
              }//in play?
            }//resolve()
          };//SpellAbility
          card.clearSpellAbility();
          card.addSpellAbility(spell);

          spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Fog") || cardName.equals("Angelsong") || cardName.equals("Darkness") ||
      		  cardName.equals("Holy Day") || cardName.equals("Lull") || cardName.equals("Moment's Peace") ||
      		  cardName.equals("Respite"))
        {
      	  SpellAbility spell = new Spell(card)
      	  {
  			private static final long serialVersionUID = -493504450911948985L;

  			public void resolve()
      		{
      			  AllZone.GameInfo.setPreventCombatDamageThisTurn(true);
      			  
      			  if (cardName.equals("Respite"))
      			  {
      				  CardList attackers = new CardList();
      				  attackers.addAll(AllZone.Combat.getAttackers());
      				  attackers.addAll(AllZone.pwCombat.getAttackers());
      				  AllZone.GameAction.getPlayerLife(card.getController()).addLife(attackers.size());
      			  }
      		}
  			public boolean canPlayAI()
  			{
  				return false;
  			}
      	  };
      	  if (card.getName().equals("Lull") || card.getName().equals("Angelsong")) {
      		  spell.setDescription("Prevent all combat damage that would be dealt this turn.");
      		  spell.setStackDescription(card.getName() + " - Prevent all combat damage that would be dealt this turn.");
      	  }
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
      		  
      	  if (cardName.equals("Moment's Peace")) {
      		  card.setFlashback(true);
      		  card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "2 G", "0"));
      	  }
        }//*************** END ************ END **************************
          
        
        //*************** START *********** START **************************
        else if (cardName.equals("Starstorm"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
 
			private static final long serialVersionUID = -3554283811532201543L;
			public void resolve()
      		{
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 1;
  								
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && CardFactoryUtil.canDamage(card, c) && 
  							   maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList humanAll = new CardList(AllZone.Human_Play.getCards());
  			    humanAll = humanAll.filter(filter);
  			    
  			    CardList compAll = new CardList(AllZone.Computer_Play.getCards());
  			    compAll = compAll.filter(filter);
  			    
  			    return humanAll.size() > (compAll.size() + 2);
  			}
      	  };
      	  spell.setDescription(cardName + " deals X damage to each creature.");
      	  spell.setStackDescription(cardName + " - deals X damage to each creature.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Vitalizing Cascade"))
        {
      	  final SpellAbility spell = new Spell(card){
  			  private static final long serialVersionUID = -5930794708688097023L;

  			  public void resolve()
      		  {
  				  AllZone.GameAction.getPlayerLife(card.getController()).addLife(card.getXManaCostPaid() + 3);
      			  card.setXManaCostPaid(0);
      		  }
      		  
      		  public boolean canPlayAI()
      		  {
      			  int humanLife = AllZone.Human_Life.getLife();
      			  int computerLife = AllZone.Computer_Life.getLife();
      			  
      			  final int maxX = ComputerUtil.getAvailableMana().size() - 1;
      			  return maxX > 3 && (humanLife >= computerLife);
      		  }
      	  };
      	  spell.setDescription("You gain X plus 3 life.");
      	  spell.setStackDescription("Vitalizing Cascade - You gain X plus 3 life.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if (cardName.equals("Reprisal")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8653455310355884536L;

                public boolean canPlayAI() {
                    CardList list = new CardList(AllZone.Human_Play.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() 
                                && c.getNetAttack() > 3 
                                && CardFactoryUtil.canTarget(card, c) 
                                && !c.getKeyword().contains("Indestructible");
                        }
                    });
                    
                    if (list.isEmpty()) return false;
              
                    CardListUtil.sortAttack(list);
                    CardListUtil.sortFlying(list);
                    setTargetCard(list.get(0));
                    return true;
                }//canPlayAI()
          
                public void resolve() {
                    if (AllZone.GameAction.isCardInPlay(getTargetCard())) {
                        AllZone.GameAction.destroyNoRegeneration(getTargetCard());
                    }
                }//resolve
            };//SpellAbility
       
            card.clearSpellAbility();
            card.addSpellAbility(spell);
       
            Input target = new Input() {
                private static final long serialVersionUID = 4794354831721082791L;
                public void showMessage() {
                    AllZone.Display.showMessage("Select target Creature to destroy");
                    ButtonUtil.enableOnlyCancel();
                }
                public void selectButtonCancel() {
                    stop();
                }
                public void selectCard(Card c, PlayerZone zone) {
                    if (zone.is(Constant.Zone.Play) 
                            && c.isCreature() 
                            && (c.getNetAttack() > 3) 
                            && CardFactoryUtil.canTarget(card, c)) {
                        spell.setTargetCard(c);
                        if (this.isFree()) 
                        {
                            this.setFree(false);
                            AllZone.Stack.add(spell);
                            stop();
                        }
                        else
                            stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//input
          
            card.setSVar("PlayMain1", "TRUE");
       
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Reinforcements")) {
           /* Put up to three target creature cards from your
            * graveyard on top of your library.
            */
           final SpellAbility spell = new Spell(card) {
             private static final long serialVersionUID = 4075591356690396548L;
             
             CardList getComputerCreatures()
             {
                 CardList list = new CardList(AllZone.Computer_Graveyard.getCards());
                 list = list.getType("Creature");
                 
                 //put biggest attack creats first
                 if (list.size()>0)
                	 CardListUtil.sortAttack(list);
                 
                 return list;
             }
             
             @Override
              public boolean canPlayAI() {
                 return getComputerCreatures().size() >= 3;
              }
             @Override
              public void resolve() {
                 String player = card.getController();
                 PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                 PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);

                 CardList creatures = new CardList(grave.getCards());
                 creatures = creatures.filter(new CardListFilter() {
                         public boolean addCard(Card c) {
                             return c.isCreature();
                         }
                     });
                 if (player.equals(Constant.Player.Human))
                 {
	                 //now, select three creatures
	                 int end = -1;
	                 end = Math.min(creatures.size(), 3);
	                 for(int i = 1; i <= end; i++) {
	                    String Title = "Put on top of library: ";
	                    if(i == 2) Title = "Put second from top of library: ";
	                    if(i == 3) Title = "Put third from top of library: ";
	                    Object o = AllZone.Display.getChoiceOptional(Title, creatures.toArray());
	                    if(o == null) break;
	                    Card c_1 = (Card) o;
	                    creatures.remove(c_1); //remove from the display list
	                    grave.remove(c_1); //remove from graveyard
	                    lib.add(c_1, i - 1);
	                 }
                 }
                 else //Computer
                 {
                	 CardList list = getComputerCreatures();
                	 int max = list.size();
                	 
                	 if (max > 3)
                		 max = 3;
                	 
                	 for (int i=0;i<max;i++)
                	 {
                		 grave.remove(list.get(i));
                		 lib.add(list.get(i), i);
                	 }
                 }
                 
              }
           };//spell
           card.clearSpellAbility();
           card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Harrow")){
            final SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -6598323179507468746L;

				@Override
                public void resolve() {
					// Put two lands into play, tapped
					AllZone.GameAction.searchLibraryTwoBasicLand(card.getController(), 
							Constant.Zone.Play, false, 
							Constant.Zone.Play, false);
                } // resolve
               
        		public void chooseTargetAI() {
        			Card target = null;
        			target = CardFactoryUtil.getWorstLand(Constant.Player.Computer);
        			setTargetCard(target);
        			AllZone.GameAction.sacrifice(getTargetCard());
        		}//chooseTargetAI()

                
                public boolean canPlayAI()
                {
                	PlayerZone library = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                	CardList list = new CardList(library.getCards());
                	list = list.getType("Basic");
                	PlayerZone inPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                	CardList listInPlay = new CardList(inPlay.getCards());
                	listInPlay = listInPlay.getType("Land");
                	// One or more lands in library, 2 or more lands in play
                	return (list.size() > 0) && (listInPlay.size() > 1);
                }
            };//SpellAbility
            Input runtime = new Input() {
                
				private static final long serialVersionUID = -7551607354431165941L;

				@Override
                public void showMessage() {
                    String player = card.getController();
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
                    CardList choice = new CardList(play.getCards());
                    choice = choice.getType("Land");   
                    
                    boolean free = false;
                    if (this.isFree())
                    	free = true;
                    
                    if (player.equals(Constant.Player.Human)) {
                    	stopSetNext(CardFactoryUtil.input_sacrifice(spell, choice, "Select a land to sacrifice.", free));
                    }
                }
            };

            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);

        } //*************** END ************ END **************************
        
      
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Natural Selection")) {
        	/* Look at the top 3 cards of target player's library and put them
        	 * back in any order. You may have that player shuffle his or
        	 * her library */

        	final SpellAbility spell = new Spell(card) {
        		private static final long serialVersionUID = 8649520296192617609L;

        		@Override
        		public void resolve() {
        			String player = getTargetPlayer();
        			AllZoneUtil.rearrangeTopOfLibrary(player, 3, false);
        			AllZone.GameAction.promptForShuffle(player);
        		}
        		@Override
        		public boolean canPlayAI() {
        			//basically the same reason as Sensei's Diving Top
        			return false;
        		}
        	};//spell
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }
        //*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Hurkyl's Recall")) {
        	/*
        	 * Return all artifacts target player owns to his or her hand.
        	 */
        	SpellAbility spell = new Spell(card) {
        		private static final long serialVersionUID = -4098702062413878046L;

        		@Override
        		public boolean canPlayAI() {
        			PlayerZone humanPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
        			CardList humanArts = new CardList(humanPlay.getCards());
        			humanArts = humanArts.getType("Artifact");
        			if(humanArts.size() > 0) {
        				return true;
        			}
        			else {
        				return false;
        			}
        		}//canPlayAI

        		@Override
        		public void chooseTargetAI() {
        			setTargetPlayer(Constant.Player.Human);
        		}//chooseTargetAI()

        		@Override
        		public void resolve() {
        			String player = getTargetPlayer();
        			PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
        			PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
        			final String opponent = AllZone.GameAction.getOpponent(player);
        			PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Play, opponent);
        			CardList artifacts = new CardList(play.getCards());
        			artifacts.addAll(oppPlay.getCards());
        			artifacts = artifacts.getType("Artifact");

        			for(int i = 0; i < artifacts.size(); i++) {
        				Card thisArtifact = artifacts.get(i);
        				//if is token, remove token from play, else return artifact to hand
        				if(thisArtifact.getOwner().equals(player)) {
        					if(thisArtifact.isToken()) {
        						play.remove(thisArtifact);
        					}
        					else {
        						AllZone.GameAction.moveTo(hand, thisArtifact);
        					}
        				}
        			}
        		}//resolve()
        	};
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }//*************** END ************ END **************************

        //*************** START *********** START **************************
        else if(cardName.equals("Fracturing Gust")) {
           /*
            * Destroy all artifacts and enchantments.
            * You gain 2 life for each permanent destroyed this way.
            */
            SpellAbility spell = new Spell(card) {
            private static final long serialVersionUID = 6940814538785932457L;

            @Override
            public void resolve() {
            	final String player = AllZone.Phase.getActivePlayer();
            	int numLifeToAdd = 0;
            	CardList all = new CardList();
            	all.addAll(AllZone.Human_Play.getCards());
            	all.addAll(AllZone.Computer_Play.getCards());
            	all = all.filter(artAndEn);

            	for(int i = 0; i < all.size(); i++) {
            		Card c = all.get(i);
            		if(AllZone.GameAction.destroy(c)) {
            			numLifeToAdd++;
            		}
            	}
            	AllZone.GameAction.getPlayerLife(player).addLife(numLifeToAdd*2);
            }// resolve()
               
                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Play.getCards());
                    CardList computer = new CardList(AllZone.Computer_Play.getCards());
                   
                    human = human.filter(artAndEn);
                    computer = computer.filter(artAndEn);                   

                    if(human.size() == 0) return false;
                   
                    // the computer will at least destroy 2 more human enchantments
                    return computer.size() < human.size() - 1
                            || (AllZone.Computer_Life.getLife() < 7 && !human.isEmpty());
                }//canPlayAI
               
                private CardListFilter artAndEn = new CardListFilter() {
                   public boolean addCard(Card c) {
                      return c.isArtifact() || c.isEnchantment();
                   }
                };
               
            };// SpellAbility
            spell.setStackDescription(card.getName() + " - destroy all artifacts and enchantments.");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Burst Lightning")) {
            final SpellAbility spell = new Spell(card) {
                
				private static final long serialVersionUID = -5191461039745723331L;
				int                       damage           = 2;
                Card                      check;
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone compHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
                    CardList hand = new CardList(compHand.getCards());
                    

                    if(AllZone.Human_Life.getLife() <= damage) return AllZone.GameAction.isCardInZone(card,
                            compHand);
                    
                    if(hand.size() >= 8) return true && AllZone.GameAction.isCardInZone(card, compHand);
                    
                    check = getFlying();
                    return check != null && AllZone.GameAction.isCardInZone(card, compHand);
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= damage) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage) return flying.get(i);
                    
                    return null;
                }
                
                @Override
                public void resolve() {
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            Card c = getTargetCard();
                            c.addDamage(damage, card);
                        }
                    } else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage,card);
                }
            };//SpellAbility
            
            spell.setDescription("Burst Lightning deals 2 damage to target creature or player. If Burst Lightning was kicked, it deals 4 damage to that creature or player instead.");
            
            final SpellAbility kicker = new Spell(card) {

				private static final long serialVersionUID = 7608486082373416819L;
				int                       damage           = 4;
                Card                      check;
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.Human_Life.getLife() <= damage) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= damage) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage) return flying.get(i);
                    
                    return null;
                }
                
                @Override
                public void resolve() {
                    
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            Card c = getTargetCard();
                            c.addDamage(damage, card);
                        }
                    } else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage,card);
                    
                    card.setKicked(true);
                }
            };//flashback
            kicker.setManaCost("R 4");
            kicker.setAdditionalManaCost("4");
            kicker.setKickerAbility(true);
            kicker.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(kicker, true, false));
            kicker.setDescription("Kicker: 4");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(kicker);
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(spell, true, false));
        }//*************** END ************ END **************************
        
        //*****************************START*******************************
        else if(cardName.equals("Twiddle")) {
        	/*
        	 * You may tap or untap target artifact, creature, or land.
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 8126471702898625866L;
				
				public boolean canPlayAI() {
        			return false;
        		}
        		public void chooseTargetAI() {
        			//setTargetCard(c);
        		}//chooseTargetAI()
        		public void resolve() {
        			if(AllZone.GameAction.isCardInPlay(getTargetCard())) {
        				if(getTargetCard().isTapped()) {
        					getTargetCard().untap();
        				}
        				else {
        					getTargetCard().tap();
        				}
        			}
        		}
        	};//SpellAbility
        	spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "Artifact;Creature;Land"));
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);		
        }//end Twiddle
        //****************END*******END***********************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Storm Seeker")) {
        	/*
        	 * Storm Seeker deals damage equal to the number of cards in target player's hand to that player.
        	 */
        	// TODO - this should be converted to keyword.  
        	// tweak spDamageTgt keyword and add "TgtPHand" or something to CardFactoryUtil.xCount()
        	SpellAbility spell = new Spell(card) {
        		private static final long serialVersionUID = -5456164079435151319L;

        		@Override
        		public void resolve() {
        			PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, getTargetPlayer());
        			int damage = hand.size();

        			//sanity check
        			if( damage < 0 )
        				damage = 0;

        			AllZone.GameAction.addDamage(getTargetPlayer(), card, damage);
        		}
        	};
        	spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());

        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));

        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if (cardName.equals("Suffer the Past"))
        {
        	final SpellAbility spell = new Spell(card){
				private static final long serialVersionUID = 1168802375190293222L;
				
				@Override
				public void resolve() {
					String tPlayer = getTargetPlayer();
					String player = card.getController();
					final int max = card.getXManaCostPaid();

					PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, tPlayer);
					CardList graveList = new CardList(grave.getCards());
					int X = Math.min(max, graveList.size());
					
					if( player.equals(Constant.Player.Human)) {
						for(int i = 0; i < X; i++) {
							Object o = AllZone.Display.getChoice("Remove from game", graveList.toArray());
							if(o == null) break;
							Card c_1 = (Card) o;
							graveList.remove(c_1); //remove from the display list
							AllZone.GameAction.removeFromGame(c_1);
						}
					}
					else { //Computer
						//Random random = new Random();
						for(int j = 0; j < X; j++) {
							//int index = random.nextInt(X-j);
							AllZone.GameAction.removeFromGame(graveList.get(j));
						}
					}

					AllZone.GameAction.getPlayerLife(tPlayer).subtractLife(X,card);
					AllZone.GameAction.getPlayerLife(player).addLife(X);
					card.setXManaCostPaid(0);
				}
				
				@Override
        		public void chooseTargetAI() {
        			setTargetPlayer(Constant.Player.Human);
        		}//chooseTargetAI()
				
				@Override
        		public boolean canPlayAI() {
        			String player = getTargetPlayer();
        			PlayerZone grave = AllZone.getZone(Constant.Zone.Library, player);
        			CardList graveList = new CardList(grave.getCards());
        			
        			//int computerLife = AllZone.Computer_Life.getLife();

        			final int maxX = ComputerUtil.getAvailableMana().size() - 1;
        			return (maxX >= 3) && (graveList.size() > 0);
        		}
        	};
        	
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Consume the Meek")) {
        	/* Destroy each creature with converted mana cost 3 or less.
        	 * They can't be regenerated.
        	 */
        	SpellAbility spell = new Spell(card) {
        		private static final long serialVersionUID = 9127892501403187034L;

        		@Override
        		public void resolve() {
        			CardList all = new CardList();
        			all.add(getHumanCreatures());
        			all.add(getComputerCreatures());

        			for(int i = 0; i < all.size(); i++) {
        				Card c = all.get(i);
        				AllZone.GameAction.destroyNoRegeneration(c);
        			}
        		}// resolve()

        		CardListFilter filter = new CardListFilter() {
        			public boolean addCard(Card c) {
        				return c.isCreature() && CardUtil.getConvertedManaCost(c) <= 3;
        			}
        		};

        		private CardList getHumanCreatures() {
        			CardList human = AllZoneUtil.getPlayerCardsInPlay(Constant.Player.Human);
        			return human.filter(filter);
        		}

        		private CardList getComputerCreatures() {
        			CardList comp = AllZoneUtil.getPlayerCardsInPlay(Constant.Player.Computer);
        			return comp.filter(filter);
        		}

        		@Override
        		public boolean canPlayAI() {
        			CardList human = getHumanCreatures();
        			human = human.getNotKeyword("Indestructible");
        			CardList computer = getComputerCreatures();
        			computer = computer.getNotKeyword("Indestructible");

        			// the computer will at least destroy 2 more human creatures
        			return  AllZone.Phase.getPhase().equals(Constant.Phase.Main2) && 
        			(computer.size() < human.size() - 1
        					|| (AllZone.Computer_Life.getLife() < 7 && !human.isEmpty()));
        		}
        	};// SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************
     
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Machinate")) {
        	/* 
        	 * Look at the top X cards of your library, where X is the number
        	 * of artifacts you control. Put one of those cards into your hand
        	 * and the rest on the bottom of your library in any order.
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 5559004016728325736L;

				@Override
        		public void resolve() {
        			String player = card.getController();
        			CardList artifacts = AllZoneUtil.getPlayerCardsInPlay(player);
        			artifacts = artifacts.getType("Artifact");
        			AllZoneUtil.rearrangeTopOfLibrary(player, artifacts.size(), false);
        		}

        		@Override
        		public boolean canPlayAI() {
        			//basically the same reason as Sensei's Diving Top
        			return false;
        		}
        	};//spell
        	spell.setStackDescription(cardName+" - Rearrange the top X cards in your library in any order.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Inferno")) {
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 4728714298882795253L;

				@Override
        		public void resolve() {
        			int damage = 6;
        			CardList all = AllZoneUtil.getCreaturesInPlay();
        			for(Card c:all) {
        				AllZone.GameAction.addDamage(c, card, damage);
        			}
        			AllZone.GameAction.addDamage(Constant.Player.Computer, card, damage);
        			AllZone.GameAction.addDamage(Constant.Player.Human, card, damage);
        		}// resolve()

        		@Override
        		public boolean canPlayAI() {
        			CardList human = AllZoneUtil.getCreaturesInPlay(Constant.Player.Human);
        			human = human.filter(powerSix);
        			human = human.getNotKeyword("Indestructible");
        			CardList computer = AllZoneUtil.getCreaturesInPlay(Constant.Player.Computer);
        			computer = computer.filter(powerSix);
        			computer = computer.getNotKeyword("Indestructible");
        			
        			// the computer will at least destroy 2 more human creatures
        			return  (AllZone.Phase.getPhase().equals(Constant.Phase.Main2) && 
        			(computer.size() < human.size() - 1
        					|| (AllZone.Computer_Life.getLife() > 6 && !human.isEmpty())))
        					|| AllZone.Human_Life.getLife() < 7;
        		}
        		
        		private CardListFilter powerSix = new CardListFilter() {
        			public boolean addCard(Card c) {
        				return c.getNetDefense() <= 6;
        			}
        		};
        	};// SpellAbility
        	spell.setStackDescription(cardName+" - Deal 6 damage to all creatures and all players.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Demonic Consultation")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1481101852928051519L;

                @Override
                public void resolve() {
                    String player = AllZone.Phase.getActivePlayer();
                    PlayerZone PlayerHand = AllZone.getZone(Constant.Zone.Hand, player);
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    final String[] input = new String[1];
                    input[0] = JOptionPane.showInputDialog(null, "Which card?", "Pick card", JOptionPane.QUESTION_MESSAGE);
                    
                    for(int i = 0; i < 7; i++) {
                        Card c = libList.get(i);
                        AllZone.GameAction.removeFromGame(c);
                    }

                    int max = libList.size();
                    int stop = 0;
                    for(int i = 0; i < max; i++) {
                        Card c = libList.get(i);
                        if(c.getName().equals(input[0])) {
                            if(stop == 0) {
                                AllZone.GameAction.moveTo(PlayerHand, c);
                                stop = 1;
                            }
                            
                        } else if(stop == 0) {
                            AllZone.GameAction.removeFromGame(c);
                        }
                    }
                }

                @Override
                public boolean canPlay() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());

                    return library.getCards().length > 6 && super.canPlay();
                }

                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility
            card.clearSpellAbility();
            spell.setStackDescription("Name a card. Exile the top six cards of your library, then reveal cards from the top of your library until you reveal the named card. Put that card into your hand and exile all other cards revealed this way");
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START ********** START *************************
        if(cardName.equals("Mana Drain"))//NOTE: The AI can't cast this spell due to inability to use a manapool, but provisions are still made for it for if/when we get to that point.
        {
        	SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6139754377230333678L;
                
                @Override
                public void resolve() {
                	SpellAbility sa = AllZone.Stack.pop();
                	
                	if(card.getController().equals(Constant.Player.Human))
                	{
                		Phase.ManaDrain_BonusMana_Human.add(CardUtil.getConvertedManaCost(sa.getSourceCard()));
                		Phase.ManaDrain_Source_Human.add(card);
                	}
                	else if(card.getController().equals(Constant.Player.Computer))
                	{
                		Phase.ManaDrain_BonusMana_AI.add(CardUtil.getConvertedManaCost(sa.getSourceCard()));
                		Phase.ManaDrain_Source_AI.add(card);        		
                	}
                }
                
                @Override
                public boolean canPlayAI()
                {
                	return false;
                }
                
                @Override
                public boolean canPlay() {
                    if(AllZone.Stack.size() != 0)
                    {
                    	return AllZone.Stack.peek().isSpell();
                    }
                    else
                    {
                    	return false;
                    }
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Kaervek's Spite")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6259614160639535500L;
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.Human_Life.getLife() <= 5) return true;
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                    
                    CardList playList = new CardList(play.getCards());
                    CardList libList = new CardList(lib.getCards());
                    
                    playList = playList.getName("Academy Rector");
                    libList = libList.getName("Barren Glory");
                    
                    return (AllZone.Human_Life.getLife() <= 5) || (playList.size() == 1 && libList.size() >= 1);
                }
                
                @Override
                public void resolve() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList list = new CardList(play.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.getName().equals("Mana Pool");
                        }
                    });
                    CardList handList = new CardList(hand.getCards());
                    
                    for(Card c:list) {
                        AllZone.GameAction.sacrifice(c);
                    }
                    AllZone.GameAction.discardRandom(card.getController(), handList.size(), this);
                    
                    PlayerLife life = AllZone.GameAction.getPlayerLife(getTargetPlayer());
                    life.subtractLife(5,card);
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            /*
            final Command sac = new Command(){
            private static final long serialVersionUID = 1643946454479782123L;

            public void execute() {
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                PlayerZone hand = AllZone.getZone(Constant.Zone.Play, card.getController());
                CardList list = new CardList(play.getCards());
                list = list.filter(new CardListFilter()
                {
                    public boolean addCard(Card c) {
                        return !c.getName().equals("Mana Pool");
                    }
                });
                CardList handList = new CardList(hand.getCards());
                
                for (Card c : list)
                {
                    AllZone.GameAction.sacrifice(c);
                }
                AllZone.GameAction.discardRandom(card.getController(), handList.size());
            }
              
            };
            */

            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        if( cardName.equals("Siren's Call") ) {
            /**
             *  Creatures the active player controls attack this turn if able.
             *  
             *  At the beginning of the next end step, destroy all non-Wall creatures
             *  that player controls that didn't attack this turn. Ignore this effect
             *  for each creature the player didn't control continuously since the
             *  beginning of the turn.
             *  
             *  Note: I cheated a bit - they are destroyed at the end of combat since
             *  the getCreatureAttackedThisCombat is cleared at the end of combat, and
             *  as far as I know, this info is not available at EndOfTurn
             */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -5746330758531799264L;

				@Override
                public boolean canPlay() {
					//can only cast during compy's turn before attackers are declared
					//Main1 phase won't work because this happens too soon I think
					return AllZone.Phase.getPhase().equals(Constant.Phase.Combat_Before_Declare_Attackers_InstantAbility);
                }//canPlay
				
				@Override
				public boolean canPlayAI() {
					return false;
				}//canPlayAI
                
                @Override
                public void resolve() {
                	//this needs to get a list of opponents creatures and set the siren flag
                	String player = card.getController();
                	String opponent = AllZone.GameAction.getOpponent(player);
                	CardList creatures = AllZoneUtil.getCreaturesInPlay(opponent);
                	for(Card creature:creatures) {
                		//skip walls, skip creatures with summoning sickness
                		//also skip creatures with haste if they came into play this turn
                		if((!creature.isWall() && !creature.hasSickness())
                				|| (creature.getKeyword().contains("Haste") && creature.getTurnInZone() != 1)) {
                			creature.setSirenAttackOrDestroy(true);
                			//System.out.println("Siren's Call - setting flag for "+creature.getName());
                		}
                	}
                	Command atEOT = new Command() {
						private static final long serialVersionUID = 5369528776959445848L;

						public void execute() {
							String player = card.getController();
							String opponent = AllZone.GameAction.getOpponent(player);
							CardList creatures = AllZoneUtil.getCreaturesInPlay(opponent);
							
							for(Card creature:creatures) {
								//System.out.println("Siren's Call - EOT - "+creature.getName() +" flag: "+creature.getSirenAttackOrDestroy());
								//System.out.println("Siren's Call - EOT - "+creature.getName() +" attacked?: "+creature.getCreatureAttackedThisCombat());
								if(creature.getSirenAttackOrDestroy() && !creature.getCreatureAttackedThisCombat()) {
									if(AllZone.GameAction.isCardInPlay(creature)) {
										//System.out.println("Siren's Call - destroying "+creature.getName());
										//this should probably go on the stack
										AllZone.GameAction.destroy(creature);
									}
								}
								creature.setSirenAttackOrDestroy(false);
							}
                        }//execute
                    };//Command
                    AllZone.EndOfCombat.addAt(atEOT);
                }//resolve
            };//SpellAbility
            spell.setStackDescription(card.getName() + " - All creatures that can attack must do so or be destroyed.");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        // -1 means keyword "Cycling" not found
        if(hasKeyword(card, "Cycling") != -1) {
            int n = hasKeyword(card, "Cycling");
            if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                
                String k[] = parse.split(":");
                final String manacost = k[1];
                
                card.addSpellAbility(CardFactoryUtil.ability_cycle(card, manacost));
            }
        }//Cycling

        while(hasKeyword(card, "TypeCycling") != -1) {
            int n = hasKeyword(card, "TypeCycling");
            if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                
                String k[] = parse.split(":");
                final String type = k[1];
                final String manacost = k[2];
                
                card.addSpellAbility(CardFactoryUtil.ability_typecycle(card, manacost, type));
            }
        }//TypeCycling
        
        if(hasKeyword(card, "Transmute") != -1) {
            int n = hasKeyword(card, "Transmute");
            if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                
                String k[] = parse.split(":");
                final String manacost = k[1];
                
                card.addSpellAbility(CardFactoryUtil.ability_transmute(card, manacost));
            }
        }//transmute
          
           
        if (card.getManaCost().contains("X"))
        {
        	SpellAbility sa = card.getSpellAbility()[0];
    		sa.setIsXCost(true);
    		
        	if (card.getManaCost().startsWith("X X"))
        		sa.setXManaCost("2");
        	else if (card.getManaCost().startsWith("X"))
        		sa.setXManaCost("1");
        }//X
         
        
        
    	return card;
    }//getCard
}
