package com.example.PokerServer.Objects;

import java.util.ArrayList;
import java.util.Collections;

public class Card implements Comparable<Card> {

    //========================================================
    //          THIS IS SERVER SIDE CARD OBJECT
    //     SOME UNNECESSARY FUNCTIONS ARE OMITTED HERE
    //========================================================


    //========================================================
    // Suits => 1 - HEART           Values =>   2
    //          2 - DIAMOND                     3
    //          3 - CLUBS                       4
    //          4 - SPADE                       5
    //                                          6
    //                                          7
    //                                          8
    //                                          9
    //                                          10
    //                                          J  (11)
    //                                          Q  (12)
    //                                          K  (13)
    //                                          A  (14)
    //========================================================


    //========================================================
    // Creating new card:
    //                  Card( suit value, value, location )
    //
    //                  suit value range = 0 to 3.......inside
    //                  manually increase 1 to make
    //                  range 1 to 4
    //
    //                  value range = 0 to 12.......inside
    //                  manually increase 2 to make
    //                  range 2 to 14
    //
    //                  location = -1 for not set
    //                  location = 0 for BOARD CARDS
    //                  location = 1 for PLAYER CARDS
    //
    //========================================================


    //============================================================================
    //              INITIALIZING PARAMETERS

    private int suit;                   //          SUIT VALUE OF CARD
    private int value;                  //          VALUE OF CARD
    private int location;               //          LOCATION VALUE OF CARDS

    private String suitName;            //          SUIT NAME
    private String valueName;           //          VALUE NAME

    //              INITIALIZING DONE
    //============================================================================


    //============================================================================
    //              CONSTRUCTORS
    //          FOR CARDS WITH NO LOCATION VALUE
    //============================================================================


    public Card(int suit, int value) {
        this.suit = suit + 1;
        this.value = value + 2;
        location = -1;

        loadNames();
    }


    //============================================================================
    //          FOR CARDS WITH GIVEN LOCATION
    //============================================================================

    public Card(int suit, int value, int location) {
        this.suit = suit + 1;
        this.value = value + 2;
        this.location = location;

        loadNames();
    }


    //=============================================================================
    //          FOR CARDS INITIALIZED WITH STRINGS
    //
    //
    //          FORMAT OF STRING   ---->    ( VALUE, SUIT VALUE, LOCATION VALUE)
    //
    //                                      (14, 2, 1)
    //                                      MEANS ACE OF DIAMOND
    //
    //=============================================================================


    public Card(String card) {

        String info[] = card.substring(1, card.length() - 1).split(",");

        value = Integer.valueOf(info[0]);
        suit = Integer.valueOf(info[1]);
        location = Integer.valueOf(info[2]);

        loadNames();
    }

    //=============================================================================


    //===========================================================================
    //
    //              FUNCTION TO LOAD CARD NAMES
    //
    //==========================================================================

    public static String getPower(ArrayList cards) {

        String ret;
        ArrayList[] valuesOfSuit = new ArrayList[5];
        ArrayList[] suitsOfValue = new ArrayList[15];

        //initializer

        for (int i = 0; i < 5; i++) valuesOfSuit[i] = new ArrayList<Card>();
        for (int i = 0; i < 15; i++) suitsOfValue[i] = new ArrayList<Card>();

        for (int i = 0; i < cards.size(); i++) {
            int s, v;
            s = ((Card) cards.get(i)).suit;
            v = ((Card) cards.get(i)).value;

            valuesOfSuit[s].add(cards.get(i));
            suitsOfValue[v].add(cards.get(i));
        }

        ret = checkRoyalFlush(valuesOfSuit);
        if (!ret.equals("")) return ret;

        ret = checkStraightFlush(valuesOfSuit);
        if (!ret.equals("")) return ret;

        ret = checkFourOfAKind(suitsOfValue);
        if (!ret.equals("")) return ret;

        ret = checkFullHouse(suitsOfValue);
        if (!ret.equals("")) return ret;

        ret = checkFlush(valuesOfSuit);
        if (!ret.equals("")) return ret;

        ret = checkStraight(suitsOfValue);
        if (!ret.equals("")) return ret;

        ret = checkThreeOfAKind(suitsOfValue);
        if (!ret.equals("")) return ret;

        ret = checkTwoPair(suitsOfValue);
        if (!ret.equals("")) return ret;

        ret = checkOnePair(suitsOfValue);
        if (!ret.equals("")) return ret;

        ret = checkHighCard(suitsOfValue);
        return ret;
    }


    //              CONSTRUCTORS DONE
    //================================================================================


    //===========================================================================
    //
    //              GETTER AND SETTERS
    //
    //===========================================================================

    private static String checkHighCard(ArrayList<Card>[] suitsOfValue) {

        String ret = "0.";

        ArrayList bleh = new ArrayList<Card>();
        for (int i = 2; i < 15; i++) {
            if (suitsOfValue[i].size() != 0) bleh.add(suitsOfValue[i].get(0));
        }
        Collections.sort(bleh);

        for (int i = 0; i < 5; i++) {
            Card temp = (Card) bleh.get(bleh.size() - i - 1);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";// temp.value;

            if (i != 4) ret += ".";
        }
        return ret;
    }

    private static String checkOnePair(ArrayList<Card>[] suitsOfValue) {

        String ret = "";
        String ret2 = "";

        int v1 = -1;

        for (int i = 14; i >= 2; i--) {

            if (suitsOfValue[i].size() < 2) continue;

            v1 = i;
            break;
        }
        if (v1 == -1) return ret;


        for (int i = 1; i >= 0; i--) {

            Card temp = suitsOfValue[v1].get(i);
            ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ").";

        }

        ret = "1." + ret2;


        int cnt = 0;

        for (int i = 14; i >= 2; i--) {

            if (i == v1 || suitsOfValue[i].size() == 0) continue;

            Card temp = suitsOfValue[i].get(0);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";//temp.value;
            cnt++;

            if (cnt < 3) ret += ".";
            else break;
        }
        return ret;
    }

    private static String checkTwoPair(ArrayList<Card>[] suitsOfValue) {

        String ret = "";
        String ret2 = "";

        int v1 = -1, v2 = -1;

        for (int i = 14; i >= 2; i--) {

            if (suitsOfValue[i].size() < 2) continue;

            if (v1 == -1) v1 = i;
            else if (v2 == -1) {
                v2 = i;
                break;
            }
        }
        if (v1 == -1 || v2 == -1) return ret;


        for (int i = 1; i >= 0; i--) {
            Card temp = suitsOfValue[v1].get(i);
            ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ").";
        }

        for (int i = 1; i >= 0; i--) {
            Card temp = suitsOfValue[v2].get(i);
            ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ").";
        }

        ret = "2." + ret2;


        for (int i = 14; i >= 2; i--) {

            if (i == v1 || i == v2 || suitsOfValue[i].size() == 0) continue;

            Card temp = suitsOfValue[i].get(suitsOfValue[i].size() - 1);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";
            break;
        }

        return ret;
    }

    private static String checkThreeOfAKind(ArrayList<Card>[] suitsOfValue) {
        String ret = "";
        String ret2 = "";

        int v = -1;
        for (int i = 14; i >= 2; i--) {

            if (suitsOfValue[i].size() == 3) {

                v = i;

                for (int j = 2; j >= 0; j--) {
                    Card temp = suitsOfValue[i].get(j);
                    ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ").";
                }
                break;
            }
        }
        if (v == -1) return ret;


        int cnt = 0;
        ret = "3." + ret2; // v + ".";

        for (int i = 14; i >= 2; i--) {

            if (v == i || suitsOfValue[i].size() == 0) continue;


            Card temp = suitsOfValue[i].get(0);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";
            cnt++;

            if (cnt < 2) ret += ".";
            else break;
        }
        return ret;
    }

    //
    //===========================================================================


    //===========================================================================
    //
    //              NECESSARY FUNCTIONS
    //
    //==========================================================================

    private static String checkStraight(ArrayList<Card>[] suitsOfValue) {

        String ret = "";

        int starter = -1;
        int ender = -1;
        int cnt = -1;

        for (int i = 14; i >= 2; i--) {

            int v = i;
            cnt = 0;

            for (int j = v; ; j--) {

                int k = j;
                if (k < 2) k += 13;

                if (suitsOfValue[k].size() == 0) break;
                cnt++;
            }
            if (cnt >= 5) {
                starter = v;
                break;
            }
        }
        if (starter == -1) return ret;


        ender = starter - cnt + 1;
        if (ender < 2) starter = ender + 5 - 1;


        ArrayList bleh = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {

            int k = starter - i;
            if (k < 2) k += 13;

            bleh.add(suitsOfValue[k].get(suitsOfValue[k].size() - 1));
        }
        Collections.sort(bleh);


        ret = "4.";
        for (int i = 0; i < 5; i++) {
            Card temp = (Card) bleh.get(bleh.size() - i - 1);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";

            if (i != 4) ret += ".";
        }

        return ret;
    }

    private static String checkFlush(ArrayList<Card>[] valuesOfSuit) {

        String ret = "";

        int s = -1;
        for (int i = 1; i < 5; i++) {
            if (valuesOfSuit[i].size() >= 5) {
                s = i;
                break;
            }
        }
        if (s == -1) return ret;


        ArrayList bleh = new ArrayList<Card>();
        for (int i = 0; i < valuesOfSuit[s].size(); i++) bleh.add(valuesOfSuit[s].get(i));

        Collections.sort(bleh);


        ret = "5.";
        for (int i = 0; i < 5; i++) {
            Card temp = (Card) bleh.get(bleh.size() - i - 1);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";
            if (i != 4) ret += ".";
        }

        return ret;
    }

    private static String checkFullHouse(ArrayList<Card>[] suitsOfValue) {

        String ret = "";

        int v1 = -1, v2 = -1;

        for (int i = 14; i >= 2; i--) {

            if (v1 != -1 && v2 != -1) break;
            if (!(suitsOfValue[i].size() >= 2)) continue;

            if (suitsOfValue[i].size() == 3) {

                if (v1 == -1) v1 = i;
                else v2 = i;

            } else if (suitsOfValue[i].size() == 2) {

                if (v2 == -1) v2 = i;

            }
        }
        if (v1 == -1 || v2 == -1) return ret;


        String ret2 = "";

        for (int i = 2; i >= 0; i--) {
            Card temp = suitsOfValue[v1].get(i);
            ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ").";
        }

        for (int i = 0; i < 2; i++) {
            Card temp = suitsOfValue[v2].get(suitsOfValue[v2].size() - 1 - i);
            ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";
            if (i != 1) ret2 += ".";
        }

        ret = "6." + ret2;
        return ret;
    }

    private static String checkFourOfAKind(ArrayList<Card>[] suitsOfValue) {

        String ret = "";
        String ret2 = "";

        int v = -1;

        for (int i = 2; i < 15; i++) {
            if (suitsOfValue[i].size() < 4) continue;

            v = i;

            for (int j = 0; j < 4; j++) {
                Card temp = suitsOfValue[i].get(suitsOfValue[i].size() - 1 - j);
                ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ").";
            }
            break;
        }
        if (v == -1) return ret;


        ret = "7." + ret2;

        for (int i = 14; i >= 2; i--) {

            if (i == v || suitsOfValue[i].size() == 0) continue;

            Card temp = (Card) suitsOfValue[i].get(suitsOfValue[i].size() - 1);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";
            break;
        }

        return ret;
    }

    private static String checkStraightFlush(ArrayList<Card>[] valuesOfSuit) {
        String ret = "";

        int s = -1;

        for (int i = 1; i <= 4; i++) {
            if (valuesOfSuit[i].size() >= 5) {
                s = i;
                break;
            }
        }
        if (s == -1) return ret;

        int starter, cnt, ender;
        starter = -1;
        cnt = -1;

        int ase[] = new int[15];
        for (int i = 0; i < 15; i++) ase[i] = -1;
        for (int i = valuesOfSuit[s].size() - 1; i >= 0; i--)
            ase[valuesOfSuit[s].get(i).value] = i;         //TAKING LOCATIONS


        for (int i = valuesOfSuit[s].size() - 1; i >= 0; i--) {

            int k;
            int v = valuesOfSuit[s].get(i).value;
            cnt = 0;

            for (int j = v; ; j--) {

                k = j;
                if (k < 2) k += 13;
                if (ase[k] == -1) break;

                cnt++;
            }
            if (cnt >= 5) {
                starter = v;
                break;
            }
        }
        if (starter == -1) return ret;


        ender = starter - cnt + 1;
        if (ender < 2) starter = ender + 5 - 1;


        ArrayList bleh = new ArrayList<Card>();

        for (int i = 0; i < 5; i++) {

            int v = starter - i;
            if (v < 2) v += 13;

            int j = ase[v];
            bleh.add(valuesOfSuit[s].get(j));
        }
        Collections.sort(bleh);


        ret = "8.";
        int sz = bleh.size();

        for (int i = 0; i < 5; i++) {

            Card temp = (Card) bleh.get(sz - i - 1);
            ret += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";

            if (i != 4) ret += ".";
        }
        return ret;
    }

    private static String checkRoyalFlush(ArrayList<Card>[] valuesOfSuit) {
        String ret = "";
        String ret2 = "";

        for (int i = 1; i < 5; i++) {                                 //FOR EACH SUIT 1 TO 4
            if (valuesOfSuit[i].size() < 5) continue;

            boolean ase = true;
            for (int j = valuesOfSuit[i].size() - 1, k = 14; j >= 0 && k >= 10; j--, k--) {         //CHECKING LAST FIVE CARDS

                Card temp = valuesOfSuit[i].get(j);

                if (temp.value != k) {
                    ase = false;
                    break;
                }

                ret2 += "(" + temp.value + "," + temp.suit + "," + temp.location + ")";
                if (k != 10) ret2 += ".";
            }

            if (ase == true) {
                ret = "9." + ret2;
                break;
            }
        }
        return ret;
    }

    //===============================================================================
    //
    //===============================================================================


    //==================================================================================================================
    //
    //          GENERATING POWER STRING FROM SEVEN CARDS
    //                                      (FIVE BOARD CARDS, TWO PLAYER CARDS)
    //
    //
    //
    //          RESULT STRING FORMAT:   powerValue.card1.card2.card3.card4.card5
    //          WHERE CARD FORMAT:      (value, suit, location value)
    //
    //          DETAILED EXPLANATION:
    //
    //          Power value stands for the power of a user cards.
    //          This is the power derived from 5 board cards and
    //          two user cards. The rule is to generate power
    //          by looking at at most 5 cards
    //
    //
    //          Power value meanings:
    //
    //                     POWERS                                       STRING EXAMPLE:
    //
    //              9   ---->   royal flush                 9.(14,4,1).(13,4,1).(12,4,0).(11,4,0).(10,4,0)
    //
    //              8   ---->   straight flush              8.(13,4,1).(12,4,0).(11,4,0).(10,4,0).(9,4,1)
    //
    //              7   ---->   four of a kind              7.(14,4,1).(14,3,1).(14,1,0).(14,2,0).(5,4,0)
    //
    //              6   ---->   full house                  6.(12,4,1).(12,3,1).(12,1,0).(5,2,0).(5,4,0)
    //
    //              5   ---->   flush                       7.(14,4,1).(12,4,1).(9,4,0).(5,4,0).(2,4,0)
    //
    //              4   ---->   straight                    7.(14,4,1).(13,3,1).(12,1,0).(3,2,0).(2,4,0)            IMPORTANT EXAMPLE
    //
    //              3   ---->   three of a kind             3.(14,4,1).(14,3,1).(14,1,0).(10,2,0).(5,4,0)
    //
    //              2   ---->   two pair                    2.(14,4,1).(14,3,1).(2,1,0).(2,2,0).(5,4,0)
    //
    //              1   ---->   one pair                    1.(14,4,1).(14,3,1).(5,1,0).(4,2,0).(2,4,0)
    //
    //              0   ---->   high card                   0.(10,4,1).(7,3,1).(5,1,0).(3,2,0).(2,4,0)
    //
    //
    //              CAUTION:
    //
    //              IN THE LISTING, ALL THE CARDS ARE SORTED ACCORDING TO THEIR PRIORITY,
    //              THEN ACCORDING TO THEIR VALUE
    //
    //
    //              LEVEL DETAILS:
    //
    //              Let all the player are the winners, then we will loop through
    //              the power value, and select winner
    //
    //              Level is the iteration, at which there was difference
    //              in powers, and we had some winners
    //
    //
    //              6.(12,4,0).(12,3,0).(12,1,0).(5,2,0).(5,4,0) -1
    //
    //              MEANING: THERE WAS A TIE AS ALL THE CARDS WERE
    //                       IN BOARD, THUS NO WINNER FOUND AND LEVEL = -1
    //
    //              6.(12,4,0).(12,3,1).(12,1,0).(5,2,0).(5,4,0) 2
    //
    //              MEANING: WE FOUND WINNER WHILE ITERATING LOCATION 2
    //                       OF THE STRING, THERE MAYBE MORE THAN ONE WINNERS
    //                       THAT WE DONT KNOW
    //
    //
    //===============================================================================


    //==============================================================================
    //
    //          PARAMETERS: SEVEN CARDS
    //                      FIVE BOARD CARDS, TWO PLAYER CARDS
    //
    //
    //          RETURN:
    //                      power ->  POWER STRING FOR USER CARDS
    //
    //
    //          RETURN STRING FORMAT:
    //
    //                      powerValue.card1.card2.card3.card4.card5
    //                      WHERE CARD FORMAT:      (value, suit, location value)
    //
    //
    //
    //
    //
    //==================================================================================================================


    //======================================================================================================================================================
    //
    //      POWERS:
    //
    //      0 -> high card              structure: 0.(serial five cards) priority one after one
    //
    //      1 -> one pair               structure: 1.(same two 1).(same two 2).(other highest three serially)
    //
    //      2 -> two pair               structure: 2.(same two 1).(same two 2)(same other two 1).(same other two 2).(other highest one)
    //
    //      3 -> three of a kind        structure: 3.(same three 1).(same three 2).(same three 3).(other highest two serially)
    //
    //      4 -> straight               structure: 4.(serial highest 1).(serial highest 2).(serial highest 3).(serial highest 4).(serial highest 5)
    //
    //      5 -> flush                  structure: 5.(suit highest 1).(suit highest 2).(suit highest 3).(suit highest 4).(suit highest 5)
    //
    //      6 -> full house             structure: 6.(same three 1).(same three 2).(same three 3).(same two 1).(same two 2)
    //
    //      7 -> four of a kind         structure: 7.(same four 1).(same four 2).(same four 3).(same four 4).(other highest one)
    //
    //      8 -> straight flush         structure: 8.(straight flush high to low serial)
    //
    //      9 -> royal flush            structure: 9.(straight royal flush high to low serial)
    //
    //
    //=========================================================================================================================================================


    //==================================================================================================================
    //
    //
    //          TWO ARRAYS ARE USED AS HASHTABLES IN getPower() FUNCTION.
    //
    //          suitsOfValue [ i ] :
    //                              AN ARRAYLIST OF CARDS WITH VALUE i
    //                              SORTED FROM LOW TO HIGH
    //
    //          valuesOfSuit [ i ] :
    //                              AN ARRAYLIST OF CARDS WITH SUIT NUMBER i
    //                              SORTED FROM LOW TO HIGH
    //
    //==================================================================================================================

    static int max(int a, int b) {
        if (a >= b) return a;
        else return b;
    }

    public static String suitMessage(String power, String lvl) {

        String ret = "";

        String[] cards = power.split("\\.");
        int level = Integer.valueOf(lvl);
        int hand = Integer.valueOf(cards[0]);

        //System.out.println(power + " " + level);

        //3.(6,4,0).(6,3,0).(6,2,1).(13,3,0).(7,2,1) 0
        // 3.(7,3,0).(7,4,0).(7,1,1).(11,1,0).(10,4,0) -1

        /*
        1.11.12.10.9 0
        1.11.12.10.9 0
        c 1.11.12.10.9 0
        a b 1.11.14.13.12 -1
        1.11.14.13.12 -1
        1.11.14.13.12 -1
        */

        if (hand == 0) {
            ret += "High Card\n";

            if (level == -1) level = 5;

            for (int i = 1; i <= level; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }
        } else if (hand == 1) {
            ret += "Pair\n";
            for (int i = 1; i <= 2; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                if (level >= 1 && level <= 2) {
                    ret += "high pair -> ";
                    Card temp = new Card(cards[level]);
                    ret += temp.toString() + "\n";
                } else {
                    for (int i = 3; i <= level; i++) {
                        Card temp = new Card(cards[i]);
                        ret += temp.toString() + ", ";
                    }
                }
            }
        } else if (hand == 2) {
            ret += "Two Pair\n";
            for (int i = 1; i <= 4; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                if (level >= 1 && level <= 4) ret += "high pair -> ";

                Card temp = new Card(cards[level]);
                ret += temp.toString() + "\n";
            }

        } else if (hand == 3) {
            ret += "Three of a kind\n";
            for (int i = 1; i <= 3; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                if (level >= 1 && level <= 3) {
                    ret += "Three of a kind -> ";

                    Card temp = new Card(cards[level]);
                    ret += temp.toString() + ", ";
                } else {
                    for (int i = 4; i <= level; i++) {
                        Card temp = new Card(cards[i]);
                        ret += temp.toString() + ", ";
                    }
                }
            }

        } else if (hand == 4) {
            ret += "Straight\n";
            for (int i = 1; i <= 5; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                for (int i = 1; i <= level; i++) {
                    Card temp = new Card(cards[i]);
                    ret += temp.toString() + ", ";
                }
            }

        } else if (hand == 5) {
            ret += "Flush\n";
            for (int i = 1; i <= 5; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                for (int i = 1; i <= level; i++) {
                    Card temp = new Card(cards[i]);
                    ret += temp.toString() + ", ";
                }
            }

        } else if (hand == 6) {
            ret += "Full house\n";
            for (int i = 1; i <= 5; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                if (level >= 1 && level <= 3) ret += "Three of a kind -> ";
                else ret += "Pair -> ";

                Card temp = new Card(cards[level]);
                ret += temp.toString() + " ";
            }

        } else if (hand == 7) {
            ret += "Four of a kind\n";
            for (int i = 1; i <= 4; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                if (level >= 1 && level <= 4) ret += "Four of a kind -> ";

                Card temp = new Card(cards[level]);
                ret += temp.toString() + "\n";
            }

        } else if (hand == 8) {
            ret += "Straight Flush\n";
            for (int i = 1; i <= 5; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }

            if (level != 0 && level != -1) {
                ret += "High card: ";

                for (int i = 1; i <= level; i++) {
                    Card temp = new Card(cards[i]);
                    ret += temp.toString() + ", ";
                }
            }
        } else if (hand == 9) {
            ret += "Royal Flush\n";
            for (int i = 1; i <= 5; i++) {
                Card temp = new Card(cards[i]);
                ret += temp.toString() + "\n";
            }
        }

        ret += "\n";
        return ret;
    }

    private void loadNames() {
        if (value >= 2 && value <= 10) valueName = String.valueOf(value);
        else if (value == 11) valueName = "J";
        else if (value == 12) valueName = "Q";
        else if (value == 13) valueName = "K";
        else if (value == 14) valueName = "A";

        if (suit == 1) suitName = "Heart";
        else if (suit == 2) suitName = "Diamond";
        else if (suit == 3) suitName = "Clubs";
        else if (suit == 4) suitName = "Spade";
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public String toStringWithoutType() {
        return valueName + " " + suitName;
    }

    @Override
    public int compareTo(Card o) {

        if (value != o.getValue()) {
            if (value > o.getValue()) return 1;
            else return -1;
        } else {
            if (location != o.getLocation()) {
                if (location > o.getLocation()) return 1;
                else return -1;
            }
            return 0;
        }
    }

    public String toString2() {
        String t = suit + "." + value;
        return t;
    }

    public String toString3() {
        String t = valueName + " " + suitName;
        return t;
    }

    @Override
    public String toString() {

        String t;
        if (location == -1) t = "Location not set";
        else if (location == 0) t = "Board card";
        else t = "Player card";

        return valueName + " " + suitName + " type: " + t;
    }

    //==================================================================================================================
    //
    //==================================================================================================================


    //=========================================================================
    //
    //
    //          THESE ARE REDUNDANT FUNCTIONS AND UNNECESSARY
    //          FOR SERVER SIDE
    //
    //          USE THEM FOR DEBUGGING, THEN REMOVE THESE FOLLOWING
    //          FUNCTIONS WHILE FINALIZING THE PROJECT
    //
    //          DESCRIPTIONS ARE GIVEN IN CLIENT SIDE CODE
    //
    //========================================================================

    public boolean compareCards(int s, int v) {
        if (suit == s + 1 && value == v + 2) return true;
        else return false;
    }

    //========================================================================
    //
    //========================================================================

}

