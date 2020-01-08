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
    double output = (1 - (
        ( (1.00*Math.sin(0.22*hr - 1.6))
        + (0.25*Math.sin(0.60*hr + 1.0)) ) + 1
      )/2.5);
    if (DEBUG) {
      System.out.printf("decay(1)=%.2f, arg1=%.2f\n", output, hr);
    }
    return output;
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

  public void init() {
    final int[] mbr = this.tools.computeBoundingBox();
    this.x_min = mbr[0];
    this.y_min = mbr[2];
    this.x_range = (mbr[1] - mbr[0]);
    this.y_range = (mbr[3] - mbr[2]);
    if (DEBUG) {
      System.out.printf("mbr={ %d, %d, %d, %d }\n",
        mbr[0], mbr[1], mbr[2], mbr[3]);
      System.out.printf("x_min=%d, y_min=%d, x_range=%d, y_range=%d\n",
        this.x_min, this.y_min, this.x_range, this.y_range);
    }
    for (int min = 0; min < 1440; min++) {
      final double var = this.decay((double) min/60);
      this.lu_distr.put(min, new MultivariateNormalDistribution(
        /* mean   */ new double[] { MU, MU },
        /* co-var */ new double[][] {
          new double[] { var, 0.0 },
          new double[] { 0.0, var } }
        )
      );
      if (DEBUG) {
        System.out.printf("put lu_distr[%d]=[#]\n", min);
      }
      double den = this.lu_distr.get(min).density(new double[] { MU, MU });
      if (den > this.d_max) this.d_max = den;
    }
  }

  public double apply(int v1, int v2, long msec) {
    final int min = Math.toIntExact(msec/1000/60 % 1440);  // truncates to the latest minute of day
    if (DEBUG) {
      System.out.printf("set min=%d\n", min);
    }
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
    if (DEBUG) {
      System.out.printf("density(1)=%.2f, arg1={ %.2f, %.2f }\n",
        output, x, y);
    }
    output /= this.d_max;  // normalize
    output = Math.max(0.1, (1 - output));  // lower-bound to 10%
    if (DEBUG) {
      System.out.printf("apply(3)=%.2f, arg1=%d, arg2=%d, arg3=%d\n",
        output, v1, v2, msec);
    }
    return output;
  }
}
