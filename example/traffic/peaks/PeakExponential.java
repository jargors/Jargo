package com.github.jargors.traffic;
import com.github.jargors.Traffic;
import com.github.jargors.exceptions.VertexNotFoundException;
import java.util.Random;
public class PeakExponential extends Traffic {
  public PeakExponential() {
    super();
    this.random.setSeed(2019); // to keep reproducibility
  }
  /**
   * @param t varying in interval [0, 86400] i.e., 24 hours, 86400 seconds
   * @return the scaled ratio based on the travel speed
   */
  public final double apply(final int v1, final int v2, final int t) {
    int lng = 0;
    int lat = 0;
    try {
      int[] coordinates = this.tools.DBQueryVertex(v1);
      lng = coordinates[0];
      lat = coordinates[1];
    } catch (VertexNotFoundException e) {
      return 1.0;
    }
    Location cur = ((lng_cmin <= lng && lng <= lng_cmax) && (lat_cmin <= lat && lat <= lat_cmax))
        ? Location.Working : Location.Residential;
    final double cor_per = cor_percentage(lng, lat, cur);
    final double noise = gen_noise.sample();
    final double lb = (0.0 + cor_per*0.5 + noise);
    final double rb = (0.5 + cor_per*0.5 + noise);
    double decay_factor = 1.0;
    if (cur == Location.NonPeak) {
      return 1; // a non-hot edge may never incur traffic jams, or Boundary edge non-peak
    } else if (cur == Location.Residential) {
      decay_factor = decay(t, mornpeak, 3200);
    } else if (cur == Location.Working) {
      decay_factor = decay(t, evenpeak, 3500);
    }
    TruncatedNormal gen = new TruncatedNormal(
        (lb + rb)/2, (0.2 + 0.6*random.nextDouble()), lb, rb);
    double speed_multip = gen.sample()*decay_factor + 1 - decay_factor;
    return Math.min(1.0, speed_multip);
  }
  private Random random = new Random();
  private final int lng_cmin = 1040375000;
  private final int lng_cmax = 1041009000;
  private final int lat_cmin = 306355000;
  private final int lat_cmax = 306889000;
  private final int lng_center = (lng_cmax + lng_cmin) / 2;
  private final int lat_center = (lat_cmax + lat_cmin) / 2;
  private final int lng_min = 1039298900;
  private final int lng_max = 1042054970;
  private final int lat_min = 305680190;
  private final int lat_max = 307870950;
  private final int mornpeak = 8*3600;
  private final int evenpeak = 18*3600;
  // return pdf(x) = standard Gaussian pdf
  public double pdf(double x) {
    return Math.exp(-x*x/2)/Math.sqrt(2*Math.PI);
  }
  // return decayed traffic jams as time goes
  public double decay(double x, double mu, double sigma) {
    return pdf((x - mu)/sigma)/pdf(0);
  }
  private TruncatedNormal gen_noise = new TruncatedNormal(0.1, 0.5, 0, 0.2);
  public double cor_percentage(int lng, int lat, Location loc) {
    if (loc == Location.Residential) { // from a non-zero value to 1 , like [0.35,1]
      double lng_p = -1, lat_p = -1;
      if (lng > lng_cmax) {
        lng_p = 1.0 * Math.abs(lng - (lng_max + lng_cmax) / 2) / (lng_max - lng_cmax);
      } else if (lng < lng_cmin) {
        lng_p = 1.0 * Math.abs(lng - (lng_cmin + lng_min) / 2) / (lng_cmin - lng_min);
      }
      if (lat > lat_cmax) {
        lat_p = 1.0 * Math.abs(lat - (lat_max + lat_cmax) / 2) / (lat_max - lat_cmax);
      } else if (lat < lat_cmin) {
        lat_p = 1.0 * Math.abs(lat - (lat_cmin + lat_min) / 2) / (lat_cmin - lat_min);
      }
      if (lng_p >= 0 && lat_p >= 0) {
        return lng_p + lat_p;
      } else if (lng_p >= 0) {
        return 2 * lng_p;
      } else {
        return 2 * lat_p;
      }
    } else if (loc == Location.Working) {
      return 1.0 * Math.abs(lng - this.lng_center) / (lng_cmax - lng_cmin) +
             1.0 * Math.abs(lat - this.lat_center) / (lat_cmax - lat_cmin);
    } else {
      System.err.println("Not concerned");
      return -1;
    }
  }
  enum Location {
    Residential, Working, NonPeak;
  }
}
