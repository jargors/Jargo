package com.github.jargors.traffic;
import com.github.jargors.Traffic;
import com.github.jargors.exceptions.VertexNotFoundException;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import java.util.Map;
import java.util.HashMap;
public class Norm1 extends Traffic {

  // The speed multipliers follow the SHAPE of the probability-density function
  // (PDF), in other words the values along the curve. We are NOT sampling from
  // the distribution itself. We use a bivariate normal centered around the
  // geographic (lng, lat) center with covariance that varies with time.
  //
  // The covariance varies by following a sinusoidal wave:

  private final double decay(final double hr) {
    return (1 - (
        ( (1.00*Math.sin(0.22*hr - 1.6))
        + (0.25*Math.sin(0.60*hr + 1.0)) ) + 1
    )/2.5);
  }

  // speed drops to 80% at 7:30 AM, virtually 0% at 9:00 AM, starts to recover
  // at 11:00 AM, recovers to 80% at 12:30 PM, steadily recovers to 100% by
  // midnight. We set bounds to f(t) so it doesn't go below 10%.

  // Pre-initialize some bivariate distributions for each hour of a day.
  private final Map<Integer, MultivariateNormalDistribution> lu_distr
    = new HashMap<Integer, MultivariateNormalDistribution>();

  // Each distribution is centered about 0.5 in both normalized dimensions
  private final double MU = 0.5;

  // For normalizing the spatial dimension
  private int x_min = 0;
  private int y_min = 0;
  private int x_range = 0;
  private int y_range = 0;

  // For normalizing the density
  private double d_max = 0;

  public Norm1(final int[] mbr) throws Exception {
    super();
    this.x_min = mbr[0];
    this.y_min = mbr[2];
    this.x_range = (mbr[1] - mbr[0]);
    this.y_range = (mbr[3] - mbr[2]);
    for (int min = 0; min < 1440; min++) {
      final double var = this.decay((double) min/60);
      this.lu_distr.put(min, new MultivariateNormalDistribution(
        /* mean   */ new double[] { MU, MU },
        /* co-var */ new double[][] {
          new double[] { var, 0.0 },
          new double[] { 0.0, var } }
        )
      );
      double den = this.lu_distr.get(min).density(new double[] { MU, MU });
      if (den > this.d_max) this.d_max = den;
    }
  }

  public double apply(int v1, int v2, int t) {
    final int min = t/60 % 1440;  // truncates to the latest minute of day
    System.out.printf("min=%d\n", min);
    double x = 0;
    double y = 0;
    try {
      final int[] coord = this.tools.DBQueryVertex(v1);
      x = (coord[0] - this.x_min) / (double) x_range;
      y = (coord[1] - this.y_min) / (double) y_range;
    } catch (VertexNotFoundException e) {
      System.err.printf("Vertex %d not found!\n", v1);
      return 1.0;
    }
    double output = this.lu_distr.get(min).density(new double[] { x, y });
    output /= this.d_max;  // normalize
    return Math.max(0.1, (1 - output));  // lower-bound to 10%
  }
}
