package com.github.jargors.traffic;
import com.github.jargors.Traffic;
import java.util.Random;
public class RandomTraffic extends Traffic {
  private Random rand = new Random();
  public double apply(int v1, int v2, int t) {
    return Math.max(0.1, rand.nextFloat());
  }
}
