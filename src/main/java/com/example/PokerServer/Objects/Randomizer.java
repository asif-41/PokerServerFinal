package com.example.PokerServer.Objects;

import java.util.ArrayList;
import java.util.Random;

public class Randomizer {

    //========================================================
    //          THIS IS SERVER SIDE RANDOMIZER OBJECT
    //          TO GENERATE RANDOM CARDS
    //========================================================


    //==========================================================
    //          INITIALIZATION
    //

    private static Random random = new Random();

    //=========================================================


    //=========================================================
    //

    public static int one() {
        return random.nextInt();
    }

    public static int one(int limit) {
        return random.nextInt(limit);
    }

    //
    //===========================================================


    //===========================================================
    //
    //      GENERATES A RANDOM VALUE
    //      THAT DOES NOT EXIST IN A GIVEN ARRAY
    //
    //==========================================================

    public static int randomUnique(int[] val, int limit) {

        if (val.length == 0) return random.nextInt(limit);

        int ret = 0;
        boolean ase = true;

        while (ase) {
            ret = random.nextInt(limit);
            ase = false;

            for (int x : val) {
                if (ret == x) {
                    ase = true;
                    break;
                }
            }
        }

        return ret;
    }

    public static int randomUnique(ArrayList val, int limit) {

        if (val.size() == 0) return random.nextInt(limit);

        int ret = 0;
        boolean ase = true;

        while (ase) {
            ret = random.nextInt(limit);
            ase = false;

            for (Object x : val) {
                if (ret == (int) x) {
                    ase = true;
                    break;
                }
            }
        }

        return ret;
    }

    //
    //===============================================================


    //===========================================================
    //
    //      GENERATES AN ARRAY OF
    //      UNIQUE RANDOM VALUES
    //
    //==========================================================


    public static int[] randSingleValueArray(int count, int limit) {

        int[] ret = new int[count];
        for (int i = 0; i < count; i++) ret[i] = -1;

        int k;
        boolean ase;

        ret[0] = random.nextInt(limit);

        for (int i = 1; i < count; i++) {

            ase = true;
            k = -1;

            while (ase) {
                k = random.nextInt(limit);
                ase = false;

                for (int j = 0; j < i; j++) {

                    if (ret[j] == k) {
                        ase = true;
                        break;
                    }
                }
            }

            ret[i] = k;
        }
        return ret;
    }

    //
    //===============================================================


    //===========================================================
    //
    //      GENERATES COUNT NUMBER OF
    //      UNIQUE CARD OBJECTS (SERVER SIDE OBJECT)
    //
    //==========================================================

    public static ArrayList randCardArray(int count) {

        int limitSuit = 4;
        int limitCards = 13;

        ArrayList ret = new ArrayList<Card>();

        int s, v;
        boolean ase;

        s = random.nextInt(limitSuit);
        v = random.nextInt(limitCards);
        ret.add(new Card(s, v));

        for (int i = 1; i < count; i++) {

            ase = true;

            while (ase) {

                s = random.nextInt(limitSuit);
                v = random.nextInt(limitCards);
                ase = false;

                for (int j = 0; j < i; j++) {

                    if (((Card) ret.get(j)).compareCards(s, v)) {
                        ase = true;
                        break;
                    }
                }
            }
            ret.add(new Card(s, v));
        }
        return ret;
    }

    //
    //============================================================================

}
