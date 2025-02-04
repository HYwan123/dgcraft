package com.example.wan_try.dglab;

public class Wave {
    public void setFrequency(int[] frequency) {
        this.frequency = frequency;
    }

    public void setStrength(int[] strength) {
        this.strength = strength;
    }

    private int[] frequency;
    private int[] strength;
    public Wave(int[] frequency,int[] strength){
        if(frequency.length != 4){
            throw new  IllegalStateException();
        }
        if(strength.length != 4){
            throw new IllegalStateException();
        }
        this.frequency = frequency;
        this.strength = strength;
    }

    public int[] getFrequency() {
        return frequency;
    }

    public int[] getStrength() {
        return strength;
    }



    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int    b : frequency) {
            builder.append(String.format("%02X", b));
        }
        for (int b : strength) {
            builder.append(String.format("%02X", b));
        }
        System.out.println(builder);
        return builder.toString();

    }
}

