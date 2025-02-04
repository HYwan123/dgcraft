package com.example.wan_try.dglab;

import java.util.ArrayList;

public class WaveSequence extends ArrayList<Wave> {
    public String toMessage(String channel) {
        StringBuilder builder = new StringBuilder();
        builder.append("pulse-").append(channel).append(":[");
        for (int i = 0; i < this.size(); i++) {
            builder.append("\"").append(this.get(i).toString()).append("\"");
            if (i < this.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    // 将所有波形的频率乘以一个系数
    public void multiplyFrequency(double factor) {
        for (Wave wave : this) {
            int[] freq = wave.getFrequency();
            int[] newFreq = new int[freq.length];
            for (int i = 0; i < freq.length; i++) {
                newFreq[i] = (int)(freq[i] * factor);
            }
            wave.setFrequency(newFreq);
        }
    }

    // 将所有波形的强度乘以一个系数
    public void multiplyStrength(double factor) {
        for (Wave wave : this) {
            int[] strength = wave.getStrength();
            int[] newStrength = new int[strength.length];
            for (int i = 0; i < strength.length; i++) {
                newStrength[i] = (int)(strength[i] * factor);
            }
            wave.setStrength(newStrength);
        }
    }

    // 将所有波形的频率加上一个值
    public void addFrequency(int value) {
        for (Wave wave : this) {
            int[] freq = wave.getFrequency();
            int[] newFreq = new int[freq.length];
            for (int i = 0; i < freq.length; i++) {
                newFreq[i] = freq[i] + value;
            }
            wave.setFrequency(newFreq);
        }
    }

    // 将所有波形的强度加上一个值
    public void addStrength(int value) {
        for (Wave wave : this) {
            int[] strength = wave.getStrength();
            int[] newStrength = new int[strength.length];
            for (int i = 0; i < strength.length; i++) {
                newStrength[i] = strength[i] + value;
            }
            wave.setStrength(newStrength);
        }
    }

    // 将两个序列的波形频率相加
    public void addFrequencyWith(WaveSequence other) {
        if (this.size() != other.size()) {
            throw new IllegalArgumentException("序列长度不一致");
        }
        for (int i = 0; i < this.size(); i++) {
            Wave thisWave = this.get(i);
            Wave otherWave = other.get(i);
            int[] thisFreq = thisWave.getFrequency();
            int[] otherFreq = otherWave.getFrequency();
            int[] newFreq = new int[thisFreq.length];
            for (int j = 0; j < thisFreq.length; j++) {
                newFreq[j] = thisFreq[j] + otherFreq[j];
            }
            thisWave.setFrequency(newFreq);
        }
    }

    // 将两个序列的波形强度相加
    public void addStrengthWith(WaveSequence other) {
        if (this.size() != other.size()) {
            throw new IllegalArgumentException("序列长度不一致");
        }
        for (int i = 0; i < this.size(); i++) {
            Wave thisWave = this.get(i);
            Wave otherWave = other.get(i);
            int[] thisStrength = thisWave.getStrength();
            int[] otherStrength = otherWave.getStrength();
            int[] newStrength = new int[thisStrength.length];
            for (int j = 0; j < thisStrength.length; j++) {
                newStrength[j] = thisStrength[j] + otherStrength[j];
            }
            thisWave.setStrength(newStrength);
        }
    }

    // 将两个序列的波形频率相乘
    public void multiplyFrequencyWith(WaveSequence other) {
        if (this.size() != other.size()) {
            throw new IllegalArgumentException("序列长度不一致");
        }
        for (int i = 0; i < this.size(); i++) {
            Wave thisWave = this.get(i);
            Wave otherWave = other.get(i);
            int[] thisFreq = thisWave.getFrequency();
            int[] otherFreq = otherWave.getFrequency();
            int[] newFreq = new int[thisFreq.length];
            for (int j = 0; j < thisFreq.length; j++) {
                newFreq[j] = thisFreq[j] * otherFreq[j];
            }
            thisWave.setFrequency(newFreq);
        }
    }

    // 将两个序列的波形强度相乘
    public void multiplyStrengthWith(WaveSequence other) {
        if (this.size() != other.size()) {
            throw new IllegalArgumentException("序列长度不一致");
        }
        for (int i = 0; i < this.size(); i++) {
            Wave thisWave = this.get(i);
            Wave otherWave = other.get(i);
            int[] thisStrength = thisWave.getStrength();
            int[] otherStrength = otherWave.getStrength();
            int[] newStrength = new int[thisStrength.length];
            for (int j = 0; j < thisStrength.length; j++) {
                newStrength[j] = thisStrength[j] * otherStrength[j];
            }
            thisWave.setStrength(newStrength);
        }
    }
}
