/*line 26 "src/Storage.nw"*/
package com.github.jargors;
/*line 32 "src/Storage.nw"*/
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import com.github.jargors.exceptions.TimeWindowException;
/*line 41 "src/Storage.nw"*/
import java.sql.CallableStatement;   import java.sql.Connection;
import java.sql.DriverManager;       import java.sql.PreparedStatement;
import java.sql.ResultSet;           import java.sql.SQLException;
import java.sql.Statement;           import java.sql.Types;
/*line 47 "src/Storage.nw"*/
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
/*line 57 "src/Storage.nw"*/
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
/*line 6 "src/Storage.nw"*/
public class Storage {
  
/*line 73 "src/Storage.nw"*/
private Map<Integer, Boolean> lu_rstatus = new HashMap<>();  //*
/*line 79 "src/Storage.nw"*/
private ConcurrentHashMap<String, String> lu_pstr     = new ConcurrentHashMap<String, String>();
private ConcurrentHashMap<Integer, int[]> lu_vertices = new ConcurrentHashMap<Integer, int[]>();
private ConcurrentHashMap<Integer,
    ConcurrentHashMap<Integer, int[]>>    lu_edges    = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>>();
private ConcurrentHashMap<Integer, int[]> lu_users    = new ConcurrentHashMap<Integer, int[]>();
private Map<Integer, Integer>             lu_lvt      = new HashMap<Integer, Integer>();
/*line 97 "src/Storage.nw"*/
private final int    STATEMENTS_MAX_COUNT   = 20;
private       int    REQUEST_TIMEOUT        = 30;
private       String CONNECTIONS_URL        = "jdbc:derby:memory:jargo;create=true";
private final String CONNECTIONS_DRIVER_URL = "jdbc:apache:commons:dbcp:";
private final String CONNECTIONS_POOL_NAME  = "jargo";
private final String CONNECTIONS_POOL_URL   = (CONNECTIONS_DRIVER_URL + CONNECTIONS_POOL_NAME);
public static final double CSHIFT           = 10000000.0;
/*line 112 "src/Storage.nw"*/
private ConnectionFactory               connection_factory;
private PoolableConnectionFactory       poolableconnection_factory;
private ObjectPool<PoolableConnection>  pool;
private PoolingDriver                   driver;
/*line 8 "src/Storage.nw"*/
  
/*line 133 "src/Storage.nw"*/
public Storage() {
  this.JargoSetupPreparedStatements();
}
/*line 9 "src/Storage.nw"*/
  
/*line 10 "src/tex/0-Overview.nw"*/
public 
/*line 50 "src/tex/2-Reading.nw"*/
int[] DBQuery(final String sql, final int ncols) throws SQLException {
  int[] output = new int[] { };
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 52 "src/tex/2-Reading.nw"*/
                        ) {
    Statement stmt = conn.createStatement(
      ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    ResultSet res = stmt.executeQuery(sql);
    if (res.last()) {
      
/*line 8 "src/tex/2-Reading.nw"*/
output = new int[(ncols*res.getRow())];
res.first();
do {
  for (int j = 1; j <= ncols; j++) {
    output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
  }
} while (res.next());
/*line 58 "src/tex/2-Reading.nw"*/
    }
    conn.close();
  } catch (SQLException e) {
    throw e;
  }
  return output;
}
/*line 11 "src/tex/0-Overview.nw"*/
public 
/*line 288 "src/tex/2-Reading.nw"*/
int[] DBQueryEdge(final int v1, final int v2) throws EdgeNotFoundException {
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  return this.lu_edges.get(v1).get(v2).clone();
}
/*line 12 "src/tex/0-Overview.nw"*/
public 
/*line 423 "src/tex/2-Reading.nw"*/
int[] DBQueryEdgeStatistics() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 424 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S65", 6);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 13 "src/tex/0-Overview.nw"*/
public 
/*line 333 "src/tex/2-Reading.nw"*/
int[] DBQueryEdges() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 334 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S137", 4);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 14 "src/tex/0-Overview.nw"*/
public 
/*line 376 "src/tex/2-Reading.nw"*/
int[] DBQueryEdgesCount() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 377 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S63", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 15 "src/tex/0-Overview.nw"*/
public 
/*line 106 "src/tex/2-Reading.nw"*/
int[] DBQueryMBR() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 107 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S64", 4);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 16 "src/tex/0-Overview.nw"*/
public 
/*line 2329 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDistanceBaseTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2330 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S111", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 17 "src/tex/0-Overview.nw"*/
public 
/*line 2374 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDistanceBaseUnassignedTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2375 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S138", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 18 "src/tex/0-Overview.nw"*/
public 
/*line 2417 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDistanceDetourTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2418 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S113", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 19 "src/tex/0-Overview.nw"*/
public 
/*line 2460 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDistanceTransitTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2461 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S115", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 20 "src/tex/0-Overview.nw"*/
public 
/*line 2503 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDurationPickupTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2504 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S119", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 21 "src/tex/0-Overview.nw"*/
public 
/*line 2546 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDurationTransitTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2547 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S121", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 22 "src/tex/0-Overview.nw"*/
public 
/*line 2589 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestDurationTravelTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2590 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S123", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 23 "src/tex/0-Overview.nw"*/
public 
/*line 2611 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricRequestTWViolationsTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2612 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S151", 2);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 24 "src/tex/0-Overview.nw"*/
public 
/*line 2141 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServerDistanceBaseTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2142 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S110", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 25 "src/tex/0-Overview.nw"*/
public 
/*line 2184 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServerDistanceCruisingTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2185 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S107", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 26 "src/tex/0-Overview.nw"*/
public 
/*line 2227 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServerDistanceServiceTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2228 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S109", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 27 "src/tex/0-Overview.nw"*/
public 
/*line 2098 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServerDistanceTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2099 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S105", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 28 "src/tex/0-Overview.nw"*/
public 
/*line 2270 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServerDurationTravelTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2271 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S117", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 29 "src/tex/0-Overview.nw"*/
public 
/*line 2292 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServerTWViolationsTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2293 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S150", 2);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 30 "src/tex/0-Overview.nw"*/
public 
/*line 2012 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricServiceRate() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2013 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S102", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 31 "src/tex/0-Overview.nw"*/
public 
/*line 2055 "src/tex/2-Reading.nw"*/
int[] DBQueryMetricUserDistanceBaseTotal() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 2056 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S103", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 32 "src/tex/0-Overview.nw"*/
public 
/*line 644 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestDistanceDetour(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 645 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S112", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 33 "src/tex/0-Overview.nw"*/
public 
/*line 682 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestDistanceTransit(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 683 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S114", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 34 "src/tex/0-Overview.nw"*/
public 
/*line 720 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestDurationPickup(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 721 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S118", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 35 "src/tex/0-Overview.nw"*/
public 
/*line 758 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestDurationTransit(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 759 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S120", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 36 "src/tex/0-Overview.nw"*/
public 
/*line 796 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestDurationTravel(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 797 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S122", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 37 "src/tex/0-Overview.nw"*/
public 
/*line 606 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestIsAssigned(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 607 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S148", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 38 "src/tex/0-Overview.nw"*/
public 
/*line 553 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestStatus(final int rid, final int t)
throws UserNotFoundException, SQLException {
  if (!this.lu_users.containsKey(rid)) {
    throw new UserNotFoundException("User "+rid+" not found.");
  }
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 558 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S133", 1, rid, t);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 39 "src/tex/0-Overview.nw"*/
public 
/*line 883 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestTimeOfArrival(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 884 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S126", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 40 "src/tex/0-Overview.nw"*/
public 
/*line 834 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestTimeOfDeparture(final int rid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 835 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S124", 1, rid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 41 "src/tex/0-Overview.nw"*/
public 
/*line 926 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestsCount() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 927 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S67", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 42 "src/tex/0-Overview.nw"*/
public 
/*line 982 "src/tex/2-Reading.nw"*/
int[] DBQueryRequestsQueued(final int t) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 983 "src/tex/2-Reading.nw"*/
                        ) {
/*line 992 "src/tex/2-Reading.nw"*/
    final int[] output = this.PSQuery(conn, "S143", 7, t, t, REQUEST_TIMEOUT);
    int[] temp1 = new int[output.length];
    int j = 0;
    for (int i = 0; i < (output.length - 6); i += 7) {
      if (this.lu_rstatus.get(output[i]) == false) {
        temp1[(j + 0)] = output[(i + 0)];
        temp1[(j + 1)] = output[(i + 1)];
        temp1[(j + 2)] = output[(i + 2)];
        temp1[(j + 3)] = output[(i + 3)];
        temp1[(j + 4)] = output[(i + 4)];
        temp1[(j + 5)] = output[(i + 5)];
        temp1[(j + 6)] = output[(i + 6)];
        j += 7;
      }
    }
/*line 1011 "src/tex/2-Reading.nw"*/
    int[] temp2 = new int[j];
    for (int i = 0; i < j; i += 7) {
      temp2[(i + 0)] = temp1[(i + 0)];
      temp2[(i + 1)] = temp1[(i + 1)];
      temp2[(i + 2)] = temp1[(i + 2)];
      temp2[(i + 3)] = temp1[(i + 3)];
      temp2[(i + 4)] = temp1[(i + 4)];
      temp2[(i + 5)] = temp1[(i + 5)];
      temp2[(i + 6)] = temp1[(i + 6)];
    }
    return temp2;
  } catch (SQLException e) {
    throw e;
  }
}
/*line 43 "src/tex/0-Overview.nw"*/
public 
/*line 1764 "src/tex/2-Reading.nw"*/
int[] DBQueryServerAssignmentsCompleted(final int sid, final int t)
throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1766 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S101", 1, t, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 44 "src/tex/0-Overview.nw"*/
public 
/*line 1720 "src/tex/2-Reading.nw"*/
int[] DBQueryServerAssignmentsPending(final int sid, final int t)
throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1722 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S100", 1, t, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 45 "src/tex/0-Overview.nw"*/
public 
/*line 1360 "src/tex/2-Reading.nw"*/
int[] DBQueryServerDistance(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1361 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S104", 1, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 46 "src/tex/0-Overview.nw"*/
public 
/*line 1448 "src/tex/2-Reading.nw"*/
int[] DBQueryServerDistanceCruising(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1449 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S106", 1, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 47 "src/tex/0-Overview.nw"*/
public 
/*line 1398 "src/tex/2-Reading.nw"*/
int[] DBQueryServerDistanceRemaining(final int sid, final int t)
throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1400 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S142", 1, sid, t);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 48 "src/tex/0-Overview.nw"*/
public 
/*line 1486 "src/tex/2-Reading.nw"*/
int[] DBQueryServerDistanceService(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1487 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S108", 1, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 49 "src/tex/0-Overview.nw"*/
public 
/*line 1524 "src/tex/2-Reading.nw"*/
int[] DBQueryServerDurationRemaining(final int sid, final int t)
throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1526 "src/tex/2-Reading.nw"*/
                        ) {
    int[] output = PSQuery(conn, "S127", 1, sid, t);
    if (output != null) {
      output[0] -= t;
    }
    return output;
  } catch (SQLException e) {
    throw e;
  }
}
/*line 50 "src/tex/0-Overview.nw"*/
public 
/*line 1578 "src/tex/2-Reading.nw"*/
int[] DBQueryServerDurationTravel(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1579 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S116", 1, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 51 "src/tex/0-Overview.nw"*/
public 
/*line 1312 "src/tex/2-Reading.nw"*/
int[] DBQueryServerLoadMax(final int sid, final int t) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1313 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S73", 1, sid, t);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 52 "src/tex/0-Overview.nw"*/
public 
/*line 1071 "src/tex/2-Reading.nw"*/
int[] DBQueryServerRoute(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1072 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S60", 2, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 53 "src/tex/0-Overview.nw"*/
public 
/*line 1124 "src/tex/2-Reading.nw"*/
int[] DBQueryServerRouteRemaining(final int sid, final int t) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1125 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S129", 2, sid, t);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 54 "src/tex/0-Overview.nw"*/
public 
/*line 1185 "src/tex/2-Reading.nw"*/
int[] DBQueryServerSchedule(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1186 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S61", 4, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 55 "src/tex/0-Overview.nw"*/
public 
/*line 1245 "src/tex/2-Reading.nw"*/
int[] DBQueryServerScheduleRemaining(final int sid, final int t)
throws SQLException {
  int[] output = new int[] { };
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1248 "src/tex/2-Reading.nw"*/
                        ) {
    int[] temp = PSQuery(conn, "S144", 3, sid, t);
    output = new int[(4*temp.length/3 + 4)];
    int j = 0;
    for (int i = 0; i < (temp.length - 2); i += 3) {
      output[(j + 0)] = temp[(i + 0)];
      output[(j + 1)] = temp[(i + 1)];
      output[(j + 2)] = 0;
      output[(j + 3)] = temp[(i + 2)];
      j += 4;
    }
    temp = PSQuery(conn, "S145", 2, sid);
    output[(j + 0)] = temp[0];
    output[(j + 1)] = temp[1];
    output[(j + 2)] = sid;
    output[(j + 3)] = 0;
  } catch (SQLException e) {
    throw e;
  }
  return output;
}
/*line 56 "src/tex/0-Overview.nw"*/
public 
/*line 1666 "src/tex/2-Reading.nw"*/
int[] DBQueryServerTimeOfArrival(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1667 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S127", 1, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 57 "src/tex/0-Overview.nw"*/
public 
/*line 1616 "src/tex/2-Reading.nw"*/
int[] DBQueryServerTimeOfDeparture(final int sid) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1617 "src/tex/2-Reading.nw"*/
                        ) {
    return PSQuery(conn, "S125", 1, sid);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 58 "src/tex/0-Overview.nw"*/
public 
/*line 1845 "src/tex/2-Reading.nw"*/
int[] DBQueryServersActive(final int t) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1846 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S134", 1, t, t, t);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 59 "src/tex/0-Overview.nw"*/
public 
/*line 1797 "src/tex/2-Reading.nw"*/
int[] DBQueryServersCount() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1798 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S66", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 60 "src/tex/0-Overview.nw"*/
public 
/*line 1886 "src/tex/2-Reading.nw"*/
int[] DBQueryServersLocations(final int t) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1887 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S59", 3, t, t, t, t);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 61 "src/tex/0-Overview.nw"*/
public 
/*line 1935 "src/tex/2-Reading.nw"*/
int[] DBQueryServersLocationsActive(final int t) throws SQLException {
  int[] output = new int[] { };
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1937 "src/tex/2-Reading.nw"*/
                        ) {
    int j = 0;
/*line 1944 "src/tex/2-Reading.nw"*/
    // Query S134 selects from CW. The query time is not expected to grow
    // because Table CW does not grow as we pre-load all the servers when we
    // load the problem instance.
    final int[] temp1 = this.PSQuery(conn, "S134", 2, t, t, t);  // <-- 10 ms/call
    output = new int[(3*(temp1.length/2))];
    for (int i = 0; i < temp1.length - 1; i += 2) {
      final int sid = temp1[(i + 0)];
      final int  te = temp1[(i + 1)];
      // Queries S135 and S147 select from W. The query time is expected to
      // grow O(log(|W|)) as we have indexes on the relevant columns and Derby
      // implements SQL indexes as B+trees (https://db.apache.org/derby/papers/btree_package.html).
      final int lvt = this.lu_lvt.get(sid);
      final int[] temp2 = (t < te
        ? this.PSQuery(conn, "S135", 2, sid, sid, lvt, t, t)  // <-- 0.07-0.15 ms/call (before lvt)
        : this.PSQuery(conn, "S147", 2, sid, sid));           // <-- 0.04-0.15 ms/call
      output[(j + 0)] = sid;
      output[(j + 1)] = temp2[0];
      output[(j + 2)] = temp2[1];
      this.lu_lvt.put(sid, temp2[0]);
      j += 3;
    }
  } catch (SQLException e) {
    throw e;
  }
  return output;
}
/*line 62 "src/tex/0-Overview.nw"*/
public 
/*line 466 "src/tex/2-Reading.nw"*/
int[] DBQueryUser(final int uid)
throws UserNotFoundException {
  if (!this.lu_users.containsKey(uid)) {
    throw new UserNotFoundException("User "+uid+" not found.");
  }
  return this.lu_users.get(uid).clone();
}
/*line 63 "src/tex/0-Overview.nw"*/
public 
/*line 515 "src/tex/2-Reading.nw"*/
int[] DBQueryUsers() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 516 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S141", 7);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 64 "src/tex/0-Overview.nw"*/
public 
/*line 153 "src/tex/2-Reading.nw"*/
int[] DBQueryVertex(final int v) throws VertexNotFoundException {
  if (!this.lu_vertices.containsKey(v)) {
    throw new VertexNotFoundException("Vertex "+v+" not found.");
  }
  int[] output = this.lu_vertices.get(v).clone();
  return new int[] { output[0], output[1], (int) Storage.CSHIFT };
}
/*line 65 "src/tex/0-Overview.nw"*/
public 
/*line 198 "src/tex/2-Reading.nw"*/
int[] DBQueryVertices() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 199 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S136", 3);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 66 "src/tex/0-Overview.nw"*/
public 
/*line 241 "src/tex/2-Reading.nw"*/
int[] DBQueryVerticesCount() throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 242 "src/tex/2-Reading.nw"*/
                        ) {
    return this.PSQuery(conn, "S62", 1);
  } catch (SQLException e) {
    throw e;
  }
}
/*line 70 "src/tex/0-Overview.nw"*/
public 
/*line 402 "src/tex/3-Writing.nw"*/
void DBInsertEdge(final int v1, final int v2, final int dd, final int nu)
throws DuplicateEdgeException, SQLException {
  if (this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2)) {
    throw new DuplicateEdgeException("Edge ("+v1+", "+v2+") already exists.");
  }
  if (!this.lu_edges.containsKey(v1)) {
    this.lu_edges.put(v1, new ConcurrentHashMap<Integer, int[]>());
  }
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 410 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      PreparedStatement pS1 = this.PSCreate(conn, "S1");
      this.PSAdd(pS1, v1, v2, dd, nu);
      this.PSSubmit(pS1);
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
  this.lu_edges.get(v1).put(v2, new int[] { dd, nu });
}
/*line 71 "src/tex/0-Overview.nw"*/
public 
/*line 508 "src/tex/3-Writing.nw"*/
void DBInsertRequest(final int[] u)
throws DuplicateUserException, SQLException {
  final int uid = u[0];
  if (this.lu_users.containsKey(uid)) {
    throw new DuplicateUserException("User "+uid+" already exists.");
  }
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 514 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      
/*line 75 "src/tex/3-Writing.nw"*/
PreparedStatement pS2 = this.PSCreate(conn, "S2");
PreparedStatement pS3 = this.PSCreate(conn, "S3");
PreparedStatement pS4 = this.PSCreate(conn, "S4");
PreparedStatement pS5 = this.PSCreate(conn, "S5");
PreparedStatement pS6 = this.PSCreate(conn, "S6");
PreparedStatement pS7 = this.PSCreate(conn, "S7");
this.PSAdd(pS2, uid, u[1]);
this.PSAdd(pS3, uid, u[2]);
this.PSAdd(pS4, uid, u[3]);
this.PSAdd(pS5, uid, u[4]);
this.PSAdd(pS6, uid, u[5]);
this.PSAdd(pS7, uid, u[6]);
this.PSSubmit(pS2, pS3, pS4, pS5, pS6, pS7);
/*line 517 "src/tex/3-Writing.nw"*/
      
/*line 92 "src/tex/3-Writing.nw"*/
PreparedStatement pS9 = this.PSCreate(conn, "S9");
this.PSAdd(pS9, uid, u[1], u[2], u[3], u[4], u[5], u[6]);
this.PSSubmit(pS9);
/*line 518 "src/tex/3-Writing.nw"*/
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 533 "src/tex/3-Writing.nw"*/
  this.lu_users.put(u[0], u.clone());
  this.lu_rstatus.put(u[0], false);
}
/*line 72 "src/tex/0-Overview.nw"*/
public 
/*line 594 "src/tex/3-Writing.nw"*/
void DBInsertServer(final int[] u, final int[] route)
throws DuplicateUserException, EdgeNotFoundException, SQLException {
  final int uid = u[0];
  if (this.lu_users.containsKey(uid)) {
    throw new DuplicateUserException("User "+uid+" already exists.");
  }
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 600 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      final int se = u[2];
      
/*line 75 "src/tex/3-Writing.nw"*/
PreparedStatement pS2 = this.PSCreate(conn, "S2");
PreparedStatement pS3 = this.PSCreate(conn, "S3");
PreparedStatement pS4 = this.PSCreate(conn, "S4");
PreparedStatement pS5 = this.PSCreate(conn, "S5");
PreparedStatement pS6 = this.PSCreate(conn, "S6");
PreparedStatement pS7 = this.PSCreate(conn, "S7");
this.PSAdd(pS2, uid, u[1]);
this.PSAdd(pS3, uid, u[2]);
this.PSAdd(pS4, uid, u[3]);
this.PSAdd(pS5, uid, u[4]);
this.PSAdd(pS6, uid, u[5]);
this.PSAdd(pS7, uid, u[6]);
this.PSSubmit(pS2, pS3, pS4, pS5, pS6, pS7);
/*line 604 "src/tex/3-Writing.nw"*/
      
/*line 99 "src/tex/3-Writing.nw"*/
PreparedStatement pS8 = this.PSCreate(conn, "S8");
this.PSAdd(pS8, uid, u[1], u[2], u[3], u[4], u[5], u[6]);
this.PSSubmit(pS8);
/*line 605 "src/tex/3-Writing.nw"*/
      
/*line 178 "src/tex/3-Writing.nw"*/
PreparedStatement pS10 = this.PSCreate(conn, "S10");
for (int i = 0; i < (route.length - 3); i += 2) {
  final int t1 = route[(i + 0)];
  final int v1 = route[(i + 1)];
  final int t2 = route[(i + 2)];
  final int v2 = route[(i + 3)];
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  final int dd = this.lu_edges.get(v1).get(v2)[0];
  final int nu = this.lu_edges.get(v1).get(v2)[1];
  this.PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
}
this.PSSubmit(pS10);
/*line 107 "src/tex/3-Writing.nw"*/
pS10 = this.PSCreate(conn, "S10");
this.PSAdd(pS10, uid, se, null, null, route[0], route[1], null, null);
this.PSSubmit(pS10);
/*line 606 "src/tex/3-Writing.nw"*/
      
/*line 120 "src/tex/3-Writing.nw"*/
PreparedStatement pS11 = this.PSCreate(conn, "S11");
final int te = route[(route.length - 2)];
this.PSAdd(pS11, uid, u[2], u[3], u[4], u[5], u[2], u[4], te, u[5]);
this.PSSubmit(pS11);
/*line 607 "src/tex/3-Writing.nw"*/
      
/*line 128 "src/tex/3-Writing.nw"*/
PreparedStatement pS14 = this.PSCreate(conn, "S14");
this.PSAdd(pS14, uid, u[1], u[2], null, u[2], u[4], null, u[1],
    null, null, null, null, null, 1);
this.PSSubmit(pS14);
/*line 608 "src/tex/3-Writing.nw"*/
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 619 "src/tex/3-Writing.nw"*/
  this.lu_users.put(uid, u.clone());
  this.lu_lvt.put(uid, 0);
}
/*line 73 "src/tex/0-Overview.nw"*/
public 
/*line 343 "src/tex/3-Writing.nw"*/
void DBInsertVertex(final int v, final int lng, final int lat)
throws DuplicateVertexException, SQLException {
/*line 351 "src/tex/3-Writing.nw"*/
  if (this.lu_vertices.containsKey(v)) {
    throw new DuplicateVertexException("Vertex "+v+" already exists.");
  }
/*line 361 "src/tex/3-Writing.nw"*/
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 361 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      PreparedStatement pS0 = this.PSCreate(conn, "S0");
      this.PSAdd(pS0, v, lng, lat);
      this.PSSubmit(pS0);
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
  this.lu_vertices.put(v, new int[] { lng, lat });
}
/*line 74 "src/tex/0-Overview.nw"*/
public 
/*line 452 "src/tex/3-Writing.nw"*/
void DBUpdateEdgeSpeed(final int v1, final int v2, final int nu)
throws EdgeNotFoundException, SQLException {
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 457 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      PreparedStatement pS15 = this.PSCreate(conn, "S15");
      PreparedStatement pS131 = this.PSCreate(conn, "S131");
      this.PSAdd(pS15, nu, v1, v2);
      this.PSAdd(pS131, nu, v1, v2);
      this.PSSubmit(pS15, pS131);
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
  this.lu_edges.get(v1).get(v2)[1] = nu;
}
/*line 75 "src/tex/0-Overview.nw"*/
public 
/*line 840 "src/tex/3-Writing.nw"*/
void DBUpdateServerAddToSchedule(
    final int sid, final int[] route, final int[] sched, final int[] rid)
throws UserNotFoundException, EdgeNotFoundException, SQLException {
  if (!this.lu_users.containsKey(sid)) {
    throw new UserNotFoundException("User "+sid+" not found.");
  }
  for (final int r : rid) {
    if (!this.lu_users.containsKey(r)) {
      throw new UserNotFoundException("User "+r+" not found.");
    }
  }
  Map<Integer, int[]> cache  = new HashMap<>();
  Map<Integer, int[]> cache2 = new HashMap<>();
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 853 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      final int sq = lu_users.get(sid)[1];
      final int se = lu_users.get(sid)[2];
      
/*line 50 "src/tex/3-Writing.nw"*/
PreparedStatement pS76 = this.PSCreate(conn, "S76");
this.PSAdd(pS76, sid, route[0]);
this.PSSubmit(pS76);
/*line 114 "src/tex/3-Writing.nw"*/
final int uid = sid;
/*line 178 "src/tex/3-Writing.nw"*/
PreparedStatement pS10 = this.PSCreate(conn, "S10");
for (int i = 0; i < (route.length - 3); i += 2) {
  final int t1 = route[(i + 0)];
  final int v1 = route[(i + 1)];
  final int t2 = route[(i + 2)];
  final int v2 = route[(i + 3)];
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  final int dd = this.lu_edges.get(v1).get(v2)[0];
  final int nu = this.lu_edges.get(v1).get(v2)[1];
  this.PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
}
this.PSSubmit(pS10);
/*line 287 "src/tex/3-Writing.nw"*/
PreparedStatement pS77 = this.PSCreate(conn, "S77");
PreparedStatement pS139 = this.PSCreate(conn, "S139");
final int te = sched[(sched.length - 3)];
final int ve = sched[(sched.length - 2)];
this.PSAdd(pS77, te, ve, sid);
this.PSAdd(pS139, te, sid);
this.PSSubmit(pS77, pS139);
/*line 858 "src/tex/3-Writing.nw"*/
      
/*line 298 "src/tex/3-Writing.nw"*/
PreparedStatement pS82 = this.PSCreate(conn, "S82");
PreparedStatement pS83 = this.PSCreate(conn, "S83");
PreparedStatement pS84 = this.PSCreate(conn, "S84");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int tj = sched[(j + 0)];
  final int vj = sched[(j + 1)];
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    this.PSAdd(pS82, tj, vj, Lj);
    this.PSAdd(pS83, tj, vj, Lj);
    this.PSAdd(pS84, tj, vj, Lj);
  }
}
this.PSSubmit(pS82, pS83, pS84);
/*line 241 "src/tex/3-Writing.nw"*/
PreparedStatement pS140 = this.PSCreate(conn, "S140");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int Lj = sched[(j + 2)];
  if (Lj != sid && !cache.containsKey(Lj)) {
    final int rq = lu_users.get(Lj)[1];
    boolean flagged = false;
    for (final int r : rid) {
      if (Lj == r) {
        flagged = true;
        break;
      }
    }
    if (flagged) {
      final int tp = sched[(j + 0)];
      final int vp = sched[(j + 1)];
      for (int k = (j + 3); k < (sched.length - 2); k += 3) {
        if (Lj == sched[(k + 2)]) {
          final int td = sched[(k + 0)];
          final int vd = sched[(k + 1)];
          cache. put(Lj, new int[] { rq, tp, td });
          cache2.put(Lj, new int[] { vp, vd });
          break;
        }
      }
    } else {
      final int[] output = this.PSQuery(conn, "S86", 2, Lj);
      final int tp = output[0];
      final int td = output[1];
      this.PSAdd(pS140, tp, td, Lj);
      cache.put(Lj, new int[] { rq, tp, td });
    }
  }
}
this.PSSubmit(pS140);
/*line 279 "src/tex/3-Writing.nw"*/
final int[] output = (route[0] == 0 ? null : this.PSQuery(conn, "S87", 3, sid, route[0]));
int t1 = (route[0] == 0 ?  0 : output[0]);
int q1 = (route[0] == 0 ? sq : output[1]);
int o1 = (route[0] == 0 ?  1 : output[2]);
/*line 68 "src/tex/3-Writing.nw"*/
PreparedStatement pS80 = this.PSCreate(conn, "S80");
this.PSAdd(pS80, sid, route[0]);
this.PSSubmit(pS80);
/*line 136 "src/tex/3-Writing.nw"*/
PreparedStatement pS14 = PSCreate(conn, "S14");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int t2 = sched[(j + 0)];
  final int v2 = sched[(j + 1)];
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    final int[] qpd = cache.get(Lj);
    final int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
    final int o2 = o1 + 1;
    this.PSAdd(pS14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
          qpd[0], qpd[1], qpd[2], o1, o2);
    t1 = t2;
    q1 = q2;
    o1 = o2;
  }
}
this.PSSubmit(pS14);
/*line 157 "src/tex/3-Writing.nw"*/
PreparedStatement pS12 = this.PSCreate(conn, "S12");
PreparedStatement pS13 = this.PSCreate(conn, "S13");
for (final int r : rid) {
  final int[] output2 = this.PSQuery(conn, "S51", 5, r);
  final int rq = output2[0];
  final int re = output2[1];
  final int rl = output2[2];
  final int ro = output2[3];
  final int rd = output2[4];
  final int[] qpd = cache.get(r);
  final int[]  pd = cache2.get(r);
  this.PSAdd(pS12, sid, qpd[1], pd[0], r);
  this.PSAdd(pS12, sid, qpd[2], pd[1], r);
  this.PSAdd(pS13, sid, se, route[(route.length - 2)], qpd[1], pd[0], qpd[2], pd[1],
        r, re, rl, ro, rd);
}
this.PSSubmit(pS12, pS13);
/*line 859 "src/tex/3-Writing.nw"*/
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 872 "src/tex/3-Writing.nw"*/
  for (final int r : rid) {
    this.lu_rstatus.put(r, true);
  }
}
/*line 76 "src/tex/0-Overview.nw"*/
public 
/*line 997 "src/tex/3-Writing.nw"*/
void DBUpdateServerRemoveFromSchedule(
    final int sid, final int[] route, final int[] sched, final int[] rid)
throws UserNotFoundException, EdgeNotFoundException, SQLException {
  if (!this.lu_users.containsKey(sid)) {
    throw new UserNotFoundException("User "+sid+" not found.");
  }
  for (final int r : rid) {
    if (!this.lu_users.containsKey(r)) {
      throw new UserNotFoundException("User "+r+" not found.");
    }
  }
  Map<Integer, int[]> cache = new HashMap<>();
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 1009 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      final int sq = lu_users.get(sid)[1];
      final int se = lu_users.get(sid)[2];
      
/*line 50 "src/tex/3-Writing.nw"*/
PreparedStatement pS76 = this.PSCreate(conn, "S76");
this.PSAdd(pS76, sid, route[0]);
this.PSSubmit(pS76);
/*line 114 "src/tex/3-Writing.nw"*/
final int uid = sid;
/*line 178 "src/tex/3-Writing.nw"*/
PreparedStatement pS10 = this.PSCreate(conn, "S10");
for (int i = 0; i < (route.length - 3); i += 2) {
  final int t1 = route[(i + 0)];
  final int v1 = route[(i + 1)];
  final int t2 = route[(i + 2)];
  final int v2 = route[(i + 3)];
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  final int dd = this.lu_edges.get(v1).get(v2)[0];
  final int nu = this.lu_edges.get(v1).get(v2)[1];
  this.PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
}
this.PSSubmit(pS10);
/*line 287 "src/tex/3-Writing.nw"*/
PreparedStatement pS77 = this.PSCreate(conn, "S77");
PreparedStatement pS139 = this.PSCreate(conn, "S139");
final int te = sched[(sched.length - 3)];
final int ve = sched[(sched.length - 2)];
this.PSAdd(pS77, te, ve, sid);
this.PSAdd(pS139, te, sid);
this.PSSubmit(pS77, pS139);
/*line 1014 "src/tex/3-Writing.nw"*/
      
/*line 298 "src/tex/3-Writing.nw"*/
PreparedStatement pS82 = this.PSCreate(conn, "S82");
PreparedStatement pS83 = this.PSCreate(conn, "S83");
PreparedStatement pS84 = this.PSCreate(conn, "S84");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int tj = sched[(j + 0)];
  final int vj = sched[(j + 1)];
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    this.PSAdd(pS82, tj, vj, Lj);
    this.PSAdd(pS83, tj, vj, Lj);
    this.PSAdd(pS84, tj, vj, Lj);
  }
}
this.PSSubmit(pS82, pS83, pS84);
/*line 222 "src/tex/3-Writing.nw"*/
PreparedStatement pS140 = this.PSCreate(conn, "S140");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    if (!cache.containsKey(Lj)) {
      final int[] output = PSQuery(conn, "S86", 2, Lj);
      final int tp = output[0];
      final int td = output[1];
      final int rq = this.lu_users.get(Lj)[1];
      cache.put(Lj, new int[] { rq, tp, td });
      this.PSAdd(pS140, tp, td, Lj);
    }
  }
}
this.PSSubmit(pS140);
/*line 279 "src/tex/3-Writing.nw"*/
final int[] output = (route[0] == 0 ? null : this.PSQuery(conn, "S87", 3, sid, route[0]));
int t1 = (route[0] == 0 ?  0 : output[0]);
int q1 = (route[0] == 0 ? sq : output[1]);
int o1 = (route[0] == 0 ?  1 : output[2]);
/*line 68 "src/tex/3-Writing.nw"*/
PreparedStatement pS80 = this.PSCreate(conn, "S80");
this.PSAdd(pS80, sid, route[0]);
this.PSSubmit(pS80);
/*line 136 "src/tex/3-Writing.nw"*/
PreparedStatement pS14 = PSCreate(conn, "S14");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int t2 = sched[(j + 0)];
  final int v2 = sched[(j + 1)];
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    final int[] qpd = cache.get(Lj);
    final int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
    final int o2 = o1 + 1;
    this.PSAdd(pS14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
          qpd[0], qpd[1], qpd[2], o1, o2);
    t1 = t2;
    q1 = q2;
    o1 = o2;
  }
}
this.PSSubmit(pS14);
/*line 1015 "src/tex/3-Writing.nw"*/
      
/*line 57 "src/tex/3-Writing.nw"*/
PreparedStatement pS42 = this.PSCreate(conn, "S42");
PreparedStatement pS43 = this.PSCreate(conn, "S43");
for (final int r : rid) {
  this.PSAdd(pS42, r);
  this.PSAdd(pS43, r);
}
this.PSSubmit(pS42, pS43);
/*line 1016 "src/tex/3-Writing.nw"*/
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 1029 "src/tex/3-Writing.nw"*/
  for (final int r : rid) {
    this.lu_rstatus.put(r, false);
  }
}
/*line 77 "src/tex/0-Overview.nw"*/
public 
/*line 712 "src/tex/3-Writing.nw"*/
void DBUpdateServerRoute(final int sid, final int[] route, final int[] sched)
throws UserNotFoundException, EdgeNotFoundException, SQLException {
  if (!this.lu_users.containsKey(sid)) {
    throw new UserNotFoundException("User "+sid+" not found.");
  }
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 717 "src/tex/3-Writing.nw"*/
                        ) {
    try {
      final int sq = lu_users.get(sid)[1];
      final int se = lu_users.get(sid)[2];
      
/*line 50 "src/tex/3-Writing.nw"*/
PreparedStatement pS76 = this.PSCreate(conn, "S76");
this.PSAdd(pS76, sid, route[0]);
this.PSSubmit(pS76);
/*line 114 "src/tex/3-Writing.nw"*/
final int uid = sid;
/*line 178 "src/tex/3-Writing.nw"*/
PreparedStatement pS10 = this.PSCreate(conn, "S10");
for (int i = 0; i < (route.length - 3); i += 2) {
  final int t1 = route[(i + 0)];
  final int v1 = route[(i + 1)];
  final int t2 = route[(i + 2)];
  final int v2 = route[(i + 3)];
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  final int dd = this.lu_edges.get(v1).get(v2)[0];
  final int nu = this.lu_edges.get(v1).get(v2)[1];
  this.PSAdd(pS10, uid, se, t1, v1, t2, v2, dd, nu);
}
this.PSSubmit(pS10);
/*line 287 "src/tex/3-Writing.nw"*/
PreparedStatement pS77 = this.PSCreate(conn, "S77");
PreparedStatement pS139 = this.PSCreate(conn, "S139");
final int te = sched[(sched.length - 3)];
final int ve = sched[(sched.length - 2)];
this.PSAdd(pS77, te, ve, sid);
this.PSAdd(pS139, te, sid);
this.PSSubmit(pS77, pS139);
/*line 722 "src/tex/3-Writing.nw"*/
      if (sched.length > 0) {
        Map<Integer, int[]> cache = new HashMap<>();
        
/*line 298 "src/tex/3-Writing.nw"*/
PreparedStatement pS82 = this.PSCreate(conn, "S82");
PreparedStatement pS83 = this.PSCreate(conn, "S83");
PreparedStatement pS84 = this.PSCreate(conn, "S84");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int tj = sched[(j + 0)];
  final int vj = sched[(j + 1)];
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    this.PSAdd(pS82, tj, vj, Lj);
    this.PSAdd(pS83, tj, vj, Lj);
    this.PSAdd(pS84, tj, vj, Lj);
  }
}
this.PSSubmit(pS82, pS83, pS84);
/*line 222 "src/tex/3-Writing.nw"*/
PreparedStatement pS140 = this.PSCreate(conn, "S140");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    if (!cache.containsKey(Lj)) {
      final int[] output = PSQuery(conn, "S86", 2, Lj);
      final int tp = output[0];
      final int td = output[1];
      final int rq = this.lu_users.get(Lj)[1];
      cache.put(Lj, new int[] { rq, tp, td });
      this.PSAdd(pS140, tp, td, Lj);
    }
  }
}
this.PSSubmit(pS140);
/*line 279 "src/tex/3-Writing.nw"*/
final int[] output = (route[0] == 0 ? null : this.PSQuery(conn, "S87", 3, sid, route[0]));
int t1 = (route[0] == 0 ?  0 : output[0]);
int q1 = (route[0] == 0 ? sq : output[1]);
int o1 = (route[0] == 0 ?  1 : output[2]);
/*line 68 "src/tex/3-Writing.nw"*/
PreparedStatement pS80 = this.PSCreate(conn, "S80");
this.PSAdd(pS80, sid, route[0]);
this.PSSubmit(pS80);
/*line 136 "src/tex/3-Writing.nw"*/
PreparedStatement pS14 = PSCreate(conn, "S14");
for (int j = 0; j < (sched.length - 2); j += 3) {
  final int t2 = sched[(j + 0)];
  final int v2 = sched[(j + 1)];
  final int Lj = sched[(j + 2)];
  if (Lj != sid) {
    final int[] qpd = cache.get(Lj);
    final int q2 = (t2 == qpd[1] ? q1 + qpd[0] : q1 - qpd[0]);
    final int o2 = o1 + 1;
    this.PSAdd(pS14, sid, sq, se, t1, t2, v2, q1, q2, Lj,
          qpd[0], qpd[1], qpd[2], o1, o2);
    t1 = t2;
    q1 = q2;
    o1 = o2;
  }
}
this.PSSubmit(pS14);
/*line 725 "src/tex/3-Writing.nw"*/
      }
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    }
  } catch (SQLException e) {
    throw e;
  }
}
/*line 81 "src/tex/0-Overview.nw"*/
public 
/*line 307 "src/tex/4-Administration.nw"*/
void JargoCacheRoadNetworkFromDB() throws SQLException {
/*line 316 "src/tex/4-Administration.nw"*/
  ConcurrentHashMap<Integer, int[]>    lu1 = new ConcurrentHashMap<Integer, int[]>();
  ConcurrentHashMap<Integer,
    ConcurrentHashMap<Integer, int[]>> lu2 = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>>();
/*line 322 "src/tex/4-Administration.nw"*/
  try {
    final int[] output = this.DBQueryVertices();
    for (int i = 0; i < (output.length - 2); i += 3) {
      final int   v = output[(i + 0)];
      final int lng = output[(i + 1)];
      final int lat = output[(i + 2)];
      lu1.put(v, new int[] { lng, lat });
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 336 "src/tex/4-Administration.nw"*/
  try {
    final int[] output = this.DBQueryEdges();
    for (int i = 0; i < (output.length - 3); i += 4) {
      final int v1 = output[(i + 0)];
      final int v2 = output[(i + 1)];
      final int dd = output[(i + 2)];
      final int nu = output[(i + 3)];
      if (!lu2.containsKey(v1)) {
        lu2.put(v1, new ConcurrentHashMap<Integer, int[]>());
      }
      lu2.get(v1).put(v2, new int[] { dd, nu });
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 354 "src/tex/4-Administration.nw"*/
  this.lu_vertices = lu1;
  this.lu_edges    = lu2;
}
/*line 82 "src/tex/0-Overview.nw"*/
public 
/*line 389 "src/tex/4-Administration.nw"*/
void JargoCacheUsersFromDB() throws SQLException {
/*line 394 "src/tex/4-Administration.nw"*/
  ConcurrentHashMap<Integer, int[]> lu1 = new ConcurrentHashMap<Integer, int[]>();
  Map<Integer, Boolean>             lu2 = new HashMap<Integer, Boolean>();
  Map<Integer, Integer>             lu3 = new HashMap<Integer, Integer>();
/*line 400 "src/tex/4-Administration.nw"*/
  try {
    final int[] output = this.DBQueryUsers();
    for (int i = 0; i < (output.length - 6); i += 7) {
      final int uid = output[(i + 0)];
      final int  uq = output[(i + 1)];
      final int  ue = output[(i + 2)];
      final int  ul = output[(i + 3)];
      final int  uo = output[(i + 4)];
      final int  ud = output[(i + 5)];
      final int  ub = output[(i + 6)];
      lu1.put(uid, new int[] { uid, uq, ue, ul, uo, ud, ub });
/*line 415 "src/tex/4-Administration.nw"*/
      if (uq > 0) {
        lu2.put(uid, (this.DBQueryRequestIsAssigned(uid).length > 0 ? true : false));
      } else {
        lu3.put(uid, 0);
      }
    }
  } catch (SQLException e) {
    throw e;
  }
/*line 427 "src/tex/4-Administration.nw"*/
  this.lu_users   = lu1;
  this.lu_rstatus = lu2;
  this.lu_lvt     = lu3;
}
/*line 83 "src/tex/0-Overview.nw"*/
public 
/*line 268 "src/tex/4-Administration.nw"*/
void JargoInstanceClose() throws SQLException {
  try {
    DriverManager.getConnection("jdbc:derby:memory:jargo;drop=true");
  } catch (SQLException e) {
    if (e.getErrorCode() != 45000) {
      throw e;
    }
  }
}
/*line 84 "src/tex/0-Overview.nw"*/
public 
/*line 230 "src/tex/4-Administration.nw"*/
void JargoInstanceExport(final String p) throws SQLException {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 231 "src/tex/4-Administration.nw"*/
                        ) {
    CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE('"+p+"')");
    cs.execute();
  } catch (SQLException e) {
    throw e;
  }
}
/*line 85 "src/tex/0-Overview.nw"*/
public 
/*line 92 "src/tex/4-Administration.nw"*/
void JargoInstanceInitialize() {
  try (
/*line 8 "src/tex/4-Administration.nw"*/
Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)
/*line 93 "src/tex/4-Administration.nw"*/
                        ) {
    Statement stmt = conn.createStatement();
    stmt.clearBatch();
    stmt.addBatch(
/*line 780 "src/tex/1-Introduction.nw"*/
"CREATE TABLE V ("
  + "v   int  CONSTRAINT P1 PRIMARY KEY,"
  + "lng int  CONSTRAINT C1 NOT NULL,"
  + "lat int  CONSTRAINT C2 NOT NULL,"
  + "CONSTRAINT C3 CHECK (lng BETWEEN -1800000000 AND 1800000000),"
  + "CONSTRAINT C4 CHECK (lat BETWEEN  -900000000 AND  900000000)"
  + ")"
/*line 96 "src/tex/4-Administration.nw"*/
                                              );
    stmt.addBatch(
/*line 791 "src/tex/1-Introduction.nw"*/
"CREATE TABLE E ("
  + "v1  int  CONSTRAINT C5 NOT NULL,"
  + "v2  int  CONSTRAINT C6 NOT NULL,"
  + "dd  int  CONSTRAINT C7 NOT NULL,"
  + "nu  int  CONSTRAINT C8 NOT NULL,"
  + "CONSTRAINT F1 FOREIGN KEY (v1) REFERENCES V (v),"
  + "CONSTRAINT F2 FOREIGN KEY (v2) REFERENCES V (v),"
  + "CONSTRAINT P2 PRIMARY KEY (v1, v2, dd, nu),"
  + "CONSTRAINT C9 CHECK (nu >= 0),"
  + "CONSTRAINT C10 CHECK (v1 <> v2),"
  + "CONSTRAINT C11 CHECK ("
  + "  CASE WHEN v1 = 0 OR v2 = 0"
  + "    THEN dd = 0"
  + "    ELSE dd > 0"
  + "  END"
  + ")"
  + ")"
/*line 97 "src/tex/4-Administration.nw"*/
                                              );
    stmt.addBatch(
/*line 841 "src/tex/1-Introduction.nw"*/
"CREATE TABLE UQ ("
  + "uid int  CONSTRAINT C12 NOT NULL,"
  + "uq  int  CONSTRAINT C13 NOT NULL,"
  + "CONSTRAINT C14 UNIQUE (uid),"
  + "CONSTRAINT C15 CHECK (uq != 0),"
  + "CONSTRAINT P3 PRIMARY KEY (uid, uq)"
  + ")"
/*line 98 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 850 "src/tex/1-Introduction.nw"*/
"CREATE TABLE UE ("
  + "uid int  CONSTRAINT C16 NOT NULL,"
  + "ue  int  CONSTRAINT C17 NOT NULL,"
  + "CONSTRAINT C18 CHECK (ue BETWEEN 0 AND 86400000),"
  + "CONSTRAINT C19 UNIQUE (uid),"
  + "CONSTRAINT P4 PRIMARY KEY (uid, ue)"
  + ")"
/*line 99 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 859 "src/tex/1-Introduction.nw"*/
"CREATE TABLE UL ("
  + "uid int  CONSTRAINT C20 NOT NULL,"
  + "ul  int  CONSTRAINT C21 NOT NULL,"
  + "CONSTRAINT C22 UNIQUE (uid),"
  + "CONSTRAINT C23 CHECK (ul BETWEEN 0 AND 86400000),"
  + "CONSTRAINT P5 PRIMARY KEY (uid, ul)"
  + ")"
/*line 100 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 868 "src/tex/1-Introduction.nw"*/
"CREATE TABLE UO ("
  + "uid int  CONSTRAINT C24 NOT NULL,"
  + "uo  int  CONSTRAINT C25 NOT NULL,"
  + "CONSTRAINT F3 FOREIGN KEY (uo) REFERENCES V (v),"
  + "CONSTRAINT C26 UNIQUE (uid),"
  + "CONSTRAINT P6 PRIMARY KEY (uid, uo)"
  + ")"
/*line 101 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 877 "src/tex/1-Introduction.nw"*/
"CREATE TABLE UD ("
  + "uid int  CONSTRAINT C27 NOT NULL,"
  + "ud  int  CONSTRAINT C28 NOT NULL,"
  + "CONSTRAINT F4 FOREIGN KEY (ud) REFERENCES V (v),"
  + "CONSTRAINT C29 UNIQUE (uid),"
  + "CONSTRAINT P7 PRIMARY KEY (uid, ud)"
  + ")"
/*line 102 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 886 "src/tex/1-Introduction.nw"*/
"CREATE TABLE UB ("
  + "uid int  CONSTRAINT C30 NOT NULL,"
  + "ub  int  CONSTRAINT C31 NOT NULL,"
  + "CONSTRAINT C32 CHECK (ub >= 0),"
  + "CONSTRAINT C33 UNIQUE (uid),"
  + "CONSTRAINT P8 PRIMARY KEY (uid, ub)"
  + ")"
/*line 103 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 1064 "src/tex/1-Introduction.nw"*/
"CREATE TABLE S ("
  + "sid int  CONSTRAINT P9 PRIMARY KEY,"
  + "sq  int  CONSTRAINT C34 NOT NULL,"
  + "se  int  CONSTRAINT C35 NOT NULL,"
  + "sl  int  CONSTRAINT C36 NOT NULL,"
  + "so  int  CONSTRAINT C37 NOT NULL,"
  + "sd  int  CONSTRAINT C38 NOT NULL,"
  + "sb  int  CONSTRAINT C39 NOT NULL,"
  + "CONSTRAINT C40 CHECK (sq < 0),"
  + "CONSTRAINT F5 FOREIGN KEY (sid, sq) REFERENCES UQ (uid, uq),"
  + "CONSTRAINT F6 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
  + "CONSTRAINT F7 FOREIGN KEY (sid, sl) REFERENCES UL (uid, ul),"
  + "CONSTRAINT F8 FOREIGN KEY (sid, so) REFERENCES UO (uid, uo),"
  + "CONSTRAINT F9 FOREIGN KEY (sid, sd) REFERENCES UD (uid, ud),"
  + "CONSTRAINT F10 FOREIGN KEY (sid, sb) REFERENCES UB (uid, ub),"
  + "CONSTRAINT C41 CHECK (se < sl)"
  + ")"
/*line 104 "src/tex/4-Administration.nw"*/
                                              );
    stmt.addBatch(
/*line 1083 "src/tex/1-Introduction.nw"*/
"CREATE TABLE R ("
  + "rid int  CONSTRAINT P10 PRIMARY KEY,"
  + "rq  int  CONSTRAINT C42 NOT NULL,"
  + "re  int  CONSTRAINT C43 NOT NULL,"
  + "rl  int  CONSTRAINT C44 NOT NULL,"
  + "ro  int  CONSTRAINT C45 NOT NULL,"
  + "rd  int  CONSTRAINT C46 NOT NULL,"
  + "rb  int  CONSTRAINT C47 NOT NULL,"
  + "CONSTRAINT C48 CHECK (rq > 0),"
  + "CONSTRAINT F11 FOREIGN KEY (rid, rq) REFERENCES UQ (uid, uq),"
  + "CONSTRAINT F12 FOREIGN KEY (rid, re) REFERENCES UE (uid, ue),"
  + "CONSTRAINT F13 FOREIGN KEY (rid, rl) REFERENCES UL (uid, ul),"
  + "CONSTRAINT F14 FOREIGN KEY (rid, ro) REFERENCES UO (uid, uo),"
  + "CONSTRAINT F15 FOREIGN KEY (rid, rd) REFERENCES UD (uid, ud),"
  + "CONSTRAINT F16 FOREIGN KEY (rid, rb) REFERENCES UB (uid, ub),"
  + "CONSTRAINT C49 CHECK (re < rl)"
  + ")"
/*line 105 "src/tex/4-Administration.nw"*/
                                              );
    stmt.addBatch(
/*line 963 "src/tex/1-Introduction.nw"*/
"CREATE TABLE W ("
  + "sid int  CONSTRAINT C50 NOT NULL,"
  + "se  int  CONSTRAINT C51 NOT NULL,"
  + "t1  int  ,"
  + "v1  int  ,"
  + "t2  int  CONSTRAINT C52 NOT NULL,"
  + "v2  int  CONSTRAINT C53 NOT NULL,"
  + "dd  int ,"
  + "nu  int ,"
  + "CONSTRAINT P11 PRIMARY KEY (sid, t2, v2),"
  + "CONSTRAINT F17 FOREIGN KEY (sid) REFERENCES S,"
  + "CONSTRAINT F18 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
  + "CONSTRAINT F19 FOREIGN KEY (v1, v2, dd, nu) REFERENCES E INITIALLY DEFERRED,"
  + "CONSTRAINT F20 FOREIGN KEY (sid, t1, v1) REFERENCES W (sid, t2, v2) INITIALLY DEFERRED,"
  + "CONSTRAINT C54 UNIQUE (sid, t1),"
  + "CONSTRAINT C55 UNIQUE (sid, t2),"
  + "CONSTRAINT C56 CHECK ("
  + "  CASE WHEN t1 IS NULL"
  + "    THEN t2 = se AND v1 IS NULL AND dd IS NULL AND nu IS NULL"
  + "    ELSE dd/(t2-t1) <= nu AND t1 < t2"
  + "  END"
  + ") INITIALLY DEFERRED"
  + ")"
/*line 106 "src/tex/4-Administration.nw"*/
                                              );
    stmt.addBatch(
/*line 1016 "src/tex/1-Introduction.nw"*/
"CREATE TABLE PD ("
  + "sid int  CONSTRAINT C57 NOT NULL,"
  + "t2  int  CONSTRAINT C58 NOT NULL,"
  + "v2  int  CONSTRAINT C59 NOT NULL,"
  + "rid int  CONSTRAINT C60 NOT NULL,"
  + "CONSTRAINT P12 PRIMARY KEY (sid, t2, v2, rid),"
  + "CONSTRAINT F21 FOREIGN KEY (sid) REFERENCES S,"
  + "CONSTRAINT F22 FOREIGN KEY (rid) REFERENCES R,"
  + "CONSTRAINT F23 FOREIGN KEY (sid, t2, v2) REFERENCES W INITIALLY DEFERRED"
  + ")"
/*line 107 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 1156 "src/tex/1-Introduction.nw"*/
"CREATE TABLE CW ("
  + "sid int  CONSTRAINT C61 NOT NULL,"
  + "se  int  CONSTRAINT C62 NOT NULL,"
  + "sl  int  CONSTRAINT C63 NOT NULL,"
  + "so  int  CONSTRAINT C64 NOT NULL,"
  + "sd  int  CONSTRAINT C65 NOT NULL,"
  + "ts  int  CONSTRAINT C66 NOT NULL,"
  + "vs  int  CONSTRAINT C67 NOT NULL,"
  + "te  int  CONSTRAINT C68 NOT NULL,"
  + "ve  int  CONSTRAINT C69 NOT NULL,"
  + "CONSTRAINT C70 UNIQUE (sid),"
  + "CONSTRAINT P13 PRIMARY KEY (sid, ts, te),"
  + "CONSTRAINT F24 FOREIGN KEY (sid) REFERENCES S,"
  + "CONSTRAINT F25 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
  + "CONSTRAINT F26 FOREIGN KEY (sid, sl) REFERENCES UL (uid, ul),"
  + "CONSTRAINT F27 FOREIGN KEY (sid, so) REFERENCES UO (uid, uo),"
  + "CONSTRAINT F28 FOREIGN KEY (sid, sd) REFERENCES UD (uid, ud),"
  + "CONSTRAINT F29 FOREIGN KEY (sid, ts, vs) REFERENCES W (sid, t2, v2) INITIALLY DEFERRED,"
  + "CONSTRAINT F30 FOREIGN KEY (sid, te, ve) REFERENCES W (sid, t2, v2) INITIALLY DEFERRED,"
  + "CONSTRAINT C71 CHECK (ts = se),"
  + "CONSTRAINT C72 CHECK (vs = so),"
//+ "CONSTRAINT C73 CHECK (te <= sl),"
  + "CONSTRAINT C74 CHECK (ve = sd),"
  + "CONSTRAINT C75 CHECK (ts < te)"
  + ")"
/*line 108 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 1250 "src/tex/1-Introduction.nw"*/
"CREATE TABLE CPD ("
  + "sid int  CONSTRAINT C76 NOT NULL,"
  + "ts  int  CONSTRAINT C77 NOT NULL,"
  + "te  int  CONSTRAINT C78 NOT NULL,"
  + "tp  int  CONSTRAINT C79 NOT NULL,"
  + "vp  int  CONSTRAINT C80 NOT NULL,"
  + "td  int  CONSTRAINT C81 NOT NULL,"
  + "vd  int  CONSTRAINT C82 NOT NULL,"
  + "rid int  CONSTRAINT C83 NOT NULL,"
  + "re  int  CONSTRAINT C84 NOT NULL,"
  + "rl  int  CONSTRAINT C85 NOT NULL,"
  + "ro  int  CONSTRAINT C86 NOT NULL,"
  + "rd  int  CONSTRAINT C87 NOT NULL,"
  + "CONSTRAINT C88 UNIQUE (rid),"
  + "CONSTRAINT P14 PRIMARY KEY (sid, tp, td, rid),"
  + "CONSTRAINT F31 FOREIGN KEY (sid) REFERENCES S,"
  + "CONSTRAINT F32 FOREIGN KEY (rid) REFERENCES R,"
  + "CONSTRAINT F33 FOREIGN KEY (sid, ts, te) REFERENCES CW (sid, ts, te) "
  + "  INITIALLY DEFERRED,"
  + "CONSTRAINT F34 FOREIGN KEY (sid, tp, vp, rid) REFERENCES PD (sid, t2, v2, rid) "
  + "  INITIALLY DEFERRED,"
  + "CONSTRAINT F35 FOREIGN KEY (sid, td, vd, rid) REFERENCES PD (sid, t2, v2, rid) "
  + "  INITIALLY DEFERRED,"
  + "CONSTRAINT F36 FOREIGN KEY (rid, re) REFERENCES UE (uid, ue),"
  + "CONSTRAINT F37 FOREIGN KEY (rid, rl) REFERENCES UL (uid, ul),"
  + "CONSTRAINT F38 FOREIGN KEY (rid, ro) REFERENCES UO (uid, uo),"
  + "CONSTRAINT F39 FOREIGN KEY (rid, rd) REFERENCES UD (uid, ud),"
  + "CONSTRAINT C89 CHECK (tp BETWEEN ts AND td) INITIALLY DEFERRED,"
  + "CONSTRAINT C90 CHECK (td BETWEEN tp AND te) INITIALLY DEFERRED,"
  + "CONSTRAINT C91 CHECK (tp >= re) INITIALLY DEFERRED,"
  + "CONSTRAINT C92 CHECK (vp  = ro) INITIALLY DEFERRED,"
//+ "CONSTRAINT C93 CHECK (td <= rl) INITIALLY DEFERRED",
  + "CONSTRAINT C94 CHECK (vd  = rd) INITIALLY DEFERRED"
  + ")"
/*line 109 "src/tex/4-Administration.nw"*/
                                                );
    stmt.addBatch(
/*line 1364 "src/tex/1-Introduction.nw"*/
"CREATE TABLE CQ ("
  + "sid int  CONSTRAINT C95 NOT NULL,"
  + "sq  int  CONSTRAINT C96 NOT NULL,"
  + "se  int  CONSTRAINT C97 NOT NULL,"
  + "t1  int  ,"
  + "t2  int  CONSTRAINT C98 NOT NULL,"
  + "v2  int  ,"
  + "q1  int  ,"
  + "q2  int  CONSTRAINT C99 NOT NULL,"
  + "rid int  ,"
  + "rq  int  ,"
  + "tp  int  ,"
  + "td  int  ,"
  + "o1  int  ,"
  + "o2  int  CONSTRAINT C100 NOT NULL,"
  + "CONSTRAINT C101 CHECK (o2 > 0),"
  + "CONSTRAINT P15 PRIMARY KEY (sid, t2, q2, o2),"
  + "CONSTRAINT F40 FOREIGN KEY (sid) REFERENCES S,"
  + "CONSTRAINT F41 FOREIGN KEY (rid) REFERENCES R,"
  + "CONSTRAINT F42 FOREIGN KEY (sid, t1, q1, o1) REFERENCES CQ (sid, t2, q2, o2)"
  + "  INITIALLY DEFERRED,"
  + "CONSTRAINT F43 FOREIGN KEY (sid, se) REFERENCES UE (uid, ue),"
  + "CONSTRAINT F44 FOREIGN KEY (sid, sq) REFERENCES UQ (uid, uq),"
  + "CONSTRAINT F45 FOREIGN KEY (rid, rq) REFERENCES UQ (uid, uq),"
  + "CONSTRAINT F46 FOREIGN KEY (sid, t2, v2, rid) REFERENCES PD INITIALLY DEFERRED,"
  + "CONSTRAINT F47 FOREIGN KEY (sid, tp, td, rid) REFERENCES CPD INITIALLY DEFERRED,"
  + "CONSTRAINT C102 CHECK ("
  + "  CASE WHEN t1 IS NULL"
  + "    THEN t2 = se AND q1 IS NULL AND q2 = sq AND o1 IS NULL AND o2 = 1"
  + "        AND rid IS NULL AND rq IS NULL AND tp IS NULL AND td IS NULL"
  + "    ELSE q2 <= 0 AND o2 = o1 + 1"
  + "  END"
  + ") INITIALLY DEFERRED,"
  + "CONSTRAINT C103 CHECK (CASE WHEN t2 = tp THEN q2 = q1 + rq END) INITIALLY DEFERRED,"
  + "CONSTRAINT C104 CHECK (CASE WHEN t2 = td THEN q2 = q1 - rq END) INITIALLY DEFERRED,"
  + "CONSTRAINT C105 UNIQUE (t2, v2, rid)"
  + ")"
/*line 110 "src/tex/4-Administration.nw"*/
                                               );
    stmt.addBatch(
/*line 1406 "src/tex/1-Introduction.nw"*/
"CREATE VIEW r_user (uid, uq, ue, ul, uo, ud, ub) AS "
  + "SELECT * from S UNION SELECT * from R"
/*line 111 "src/tex/4-Administration.nw"*/
                                                   );
    stmt.addBatch(
/*line 1414 "src/tex/1-Introduction.nw"*/
"CREATE VIEW r_server (sid, t, v, Ls, Lr) AS "
  + "SELECT W.sid, W.t2, W.v2, CW.sid, PD.rid "
  + "FROM W LEFT OUTER JOIN CW ON W.sid = CW.sid AND (W.t2 = CW.ts OR W.t2 = CW.te) "
  + "  LEFT OUTER JOIN PD ON W.sid = PD.sid AND W.t2 = PD.t2"
/*line 112 "src/tex/4-Administration.nw"*/
                                                     );
    stmt.addBatch(
/*line 1427 "src/tex/1-Introduction.nw"*/
"CREATE VIEW f_distance_blocks (sid, val, dtype) AS "
  + "SELECT d.sid, SUM (d.dd), d.dtype FROM ("
  + "  SELECT c.sid, c.dd, c.q2=c.sq as dtype FROM ("
  + "    SELECT b.sid, b.dd, b.q2, b.sq FROM ("
  + "      SELECT W.sid, W.t2, COALESCE (W.dd, 0) as dd, CQ.q2, CQ.sq, CQ.o2 "
  + "      FROM W LEFT OUTER JOIN CQ ON W.sid = CQ.sid AND W.t2 > CQ.t2"
  + "    ) AS b JOIN ("
  + "      SELECT W.sid, W.t2, MAX (CQ.o2) AS oprev "
  + "      FROM W LEFT OUTER JOIN CQ ON W.sid = CQ.sid AND W.t2 > CQ.t2 "
  + "      GROUP BY W.sid, W.t2"
  + "    ) AS a "
  + "    ON b.sid = a.sid AND b.t2 = a.t2 AND b.o2 = a.oprev"
  + "  ) AS c"
  + ") AS d "
  + "GROUP BY d.sid, d.dtype"
/*line 113 "src/tex/4-Administration.nw"*/
                                                               );
    stmt.addBatch(
/*line 1452 "src/tex/1-Introduction.nw"*/
"CREATE VIEW f_status (t, sid, rid, val) AS "
  + "SELECT a.t2, a.sid, a.rid, COUNT (b.rid) "
  + "FROM CQ AS a INNER JOIN CQ AS b ON a.t2 >= b.t2 "
  + "WHERE a.rid IS NOT NULL AND b.rid IS NOT NULL AND a.rid = b.rid "
  + "GROUP BY a.t2, a.sid, a.rid"
/*line 114 "src/tex/4-Administration.nw"*/
                                                                           );
    stmt.addBatch(
/*line 1465 "src/tex/1-Introduction.nw"*/
"CREATE VIEW assignments (t, sid, rid) AS "
  + "SELECT t, sid, rid FROM f_status WHERE val = 2 ORDER BY t ASC"
/*line 115 "src/tex/4-Administration.nw"*/
                                                                                  );
    stmt.addBatch(
/*line 1470 "src/tex/1-Introduction.nw"*/
"CREATE VIEW assignments_r (t, rid) AS "
  + "SELECT t, rid FROM assignments"
/*line 116 "src/tex/4-Administration.nw"*/
                                                                                           );
    stmt.addBatch(
/*line 1475 "src/tex/1-Introduction.nw"*/
"CREATE VIEW service_rate (val) AS "
  + "SELECT CAST(CAST(A.NUM AS FLOAT) / CAST(A.DENOM AS FLOAT) * 10000 as INT)"
  + "FROM ( "
  + "SELECT (SELECT COUNT(*) FROM assignments_r) AS NUM, "
  + "       (SELECT COUNT(*) FROM R) AS DENOM "
  + "       FROM assignments_r FETCH FIRST ROW ONLY "
  + ") A"
/*line 117 "src/tex/4-Administration.nw"*/
                                                                                     );
    stmt.addBatch(
/*line 1485 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_base (val) AS "
  + "SELECT SUM (ub) FROM UB"
/*line 118 "src/tex/4-Administration.nw"*/
                                                                                   );
    stmt.addBatch(
/*line 1490 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_s_travel (sid, val) AS "
  + "SELECT W.sid, SUM (COALESCE (dd, 0)) "
  + "FROM W JOIN CW ON w.sid = cw.sid AND (t2 BETWEEN ts AND te) "
  + "GROUP BY W.sid"
/*line 119 "src/tex/4-Administration.nw"*/
                                                           );
    stmt.addBatch(
/*line 1497 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_s_cruising (sid, val) AS "
  + "SELECT sid, val FROM f_distance_blocks WHERE dtype = true"
/*line 120 "src/tex/4-Administration.nw"*/
                                                                                              );
    stmt.addBatch(
/*line 1502 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_s_service (sid, val) AS "
  + "SELECT sid, val FROM f_distance_blocks WHERE dtype = false"
/*line 121 "src/tex/4-Administration.nw"*/
                                                                                            );
    stmt.addBatch(
/*line 1507 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_s_base (val) AS "
  + "SELECT SUM (sb) FROM S"
/*line 122 "src/tex/4-Administration.nw"*/
                                                         );
    stmt.addBatch(
/*line 1512 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_r_base (val) AS "
  + "SELECT SUM (rb) FROM R"
/*line 123 "src/tex/4-Administration.nw"*/
                                                         );
    stmt.addBatch(
/*line 1517 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_r_unassigned (val) AS "
  + "SELECT SUM (rb) FROM R LEFT JOIN assignments_r "
  + "  ON R.rid = assignments_r.rid "
  + "WHERE assignments_r.rid IS NULL"
/*line 124 "src/tex/4-Administration.nw"*/
                                                               );
    stmt.addBatch(
/*line 1529 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_r_transit (rid, val) AS "
  + "SELECT rid, SUM (COALESCE (dd, 0)) "
  + "FROM CPD JOIN W ON CPD.sid = W.sid AND CPD.tp < W.t2 AND W.t2 <= CPD.td "
  + "GROUP BY rid"
/*line 125 "src/tex/4-Administration.nw"*/
                                                                                            );
    stmt.addBatch(
/*line 1524 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dist_r_detour (rid, val) AS "
  + "SELECT rid, val-ub FROM UB JOIN dist_r_transit ON uid = rid"
/*line 126 "src/tex/4-Administration.nw"*/
                                                                                          );
    stmt.addBatch(
/*line 1536 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dur_s_travel (sid, val) AS "
  + "SELECT sid, te - ts FROM CW"
/*line 127 "src/tex/4-Administration.nw"*/
                                                          );
    stmt.addBatch(
/*line 1541 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dur_r_pickup (rid, val) AS "
  + "SELECT rid, tp - re FROM CPD"
/*line 128 "src/tex/4-Administration.nw"*/
                                                                                       );
    stmt.addBatch(
/*line 1546 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dur_r_transit (rid, val) AS "
  + "SELECT rid, td - tp FROM CPD"
/*line 129 "src/tex/4-Administration.nw"*/
                                                                                           );
    stmt.addBatch(
/*line 1551 "src/tex/1-Introduction.nw"*/
"CREATE VIEW dur_r_travel (rid, val) AS "
  + "SELECT rid, td - re FROM CPD"
/*line 130 "src/tex/4-Administration.nw"*/
                                                                                         );
    stmt.addBatch(
/*line 1556 "src/tex/1-Introduction.nw"*/
"CREATE VIEW t_r_depart (rid, val) AS "
  + "SELECT rid, tp FROM CPD"
/*line 131 "src/tex/4-Administration.nw"*/
                                                                                      );
    stmt.addBatch(
/*line 1561 "src/tex/1-Introduction.nw"*/
"CREATE VIEW t_s_depart (sid, val) AS "
  + "SELECT sid, ts FROM CW"
/*line 132 "src/tex/4-Administration.nw"*/
                                                                                      );
    stmt.addBatch(
/*line 1566 "src/tex/1-Introduction.nw"*/
"CREATE VIEW t_r_arrive (rid, val) AS "
  + "SELECT rid, td FROM CPD"
/*line 133 "src/tex/4-Administration.nw"*/
                                                                                    );
    stmt.addBatch(
/*line 1571 "src/tex/1-Introduction.nw"*/
"CREATE VIEW t_s_arrive (sid, val) AS "
  + "SELECT sid, te FROM CW"
/*line 134 "src/tex/4-Administration.nw"*/
                                                                                    );
    stmt.addBatch(
/*line 1576 "src/tex/1-Introduction.nw"*/
"CREATE VIEW violations_t_s (sid, val) AS "
  + "SELECT sid, te - sl FROM CW WHERE te - sl > 0"
/*line 135 "src/tex/4-Administration.nw"*/
                                                  );
    stmt.addBatch(
/*line 1581 "src/tex/1-Introduction.nw"*/
"CREATE VIEW violations_t_r (rid, val) AS "
  + "SELECT rid, td - rl FROM CPD WHERE td - rl > 0"
/*line 136 "src/tex/4-Administration.nw"*/
                                                  );
    stmt.addBatch("CREATE INDEX W_sid_t1 ON W (sid, t1)");
    stmt.addBatch("CREATE INDEX W_sid_t2 ON W (sid, t2)");
    stmt.addBatch("CREATE INDEX W_sid_v2 ON W (sid, v2)");
    stmt.addBatch("CREATE INDEX W_sid_t1_t2 ON W (sid, t1, t2)");
    stmt.executeBatch();
    conn.commit();
  } catch (SQLException e) {
    System.err.println("Fatal error.");
    if (e.getErrorCode() == 0) {
      System.err.println("(did you forget to call Storage.JargoInstanceNew()?)");
    } else if (e.getErrorCode() == 20000) {
      System.err.println("(data model already exists from Storage.JargoInstanceLoad()?)");
    }
    e.printStackTrace(System.err);
    System.exit(1);
  }
}
/*line 86 "src/tex/0-Overview.nw"*/
public 
/*line 190 "src/tex/4-Administration.nw"*/
void JargoInstanceLoad(final String p) throws SQLException {
  this.CONNECTIONS_URL = "jdbc:derby:memory:jargo;createFrom="+p;
  try {
    this.JargoSetupDriver();
  } catch (ClassNotFoundException e) {
    System.out.println("Fatal error.");
    e.printStackTrace();
    System.exit(1);
  }
}
/*line 87 "src/tex/0-Overview.nw"*/
public 
/*line 46 "src/tex/4-Administration.nw"*/
void JargoInstanceNew() throws SQLException {
  try {
    this.JargoSetupDriver();
  } catch (SQLException e) {
    throw e;
  } catch (ClassNotFoundException e) {
    System.err.println("Fatail exception");
    e.printStackTrace();
    System.exit(1);
  }
}
/*line 88 "src/tex/0-Overview.nw"*/
public 
/*line 994 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> getRefCacheEdges() {
  return this.lu_edges;
}
/*line 89 "src/tex/0-Overview.nw"*/
public 
/*line 1013 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, int[]> getRefCacheUsers() {
  return this.lu_users;
}
/*line 90 "src/tex/0-Overview.nw"*/
public 
/*line 1033 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, int[]> getRefCacheVertices() {
  return this.lu_vertices;
}
/*line 91 "src/tex/0-Overview.nw"*/
public 
/*line 1127 "src/tex/4-Administration.nw"*/
void setRequestTimeout(final int request_timeout) {
  this.REQUEST_TIMEOUT = request_timeout;
}
/*line 92 "src/tex/0-Overview.nw"*/
private 
/*line 465 "src/tex/4-Administration.nw"*/
void JargoSetupDriver() throws SQLException, ClassNotFoundException {
  connection_factory = new DriverManagerConnectionFactory(CONNECTIONS_URL);
  poolableconnection_factory = new PoolableConnectionFactory(connection_factory, null);
  poolableconnection_factory.setPoolStatements(true);
  poolableconnection_factory.setDefaultAutoCommit(false);
  poolableconnection_factory.setMaxOpenPreparedStatements(STATEMENTS_MAX_COUNT);
  GenericObjectPoolConfig<PoolableConnection> cfg = new GenericObjectPoolConfig<PoolableConnection>();
  cfg.setMinIdle(100000);
  cfg.setMaxIdle(100000);
  cfg.setMaxTotal(100000);
  pool = new GenericObjectPool<PoolableConnection>(poolableconnection_factory, cfg);
  poolableconnection_factory.setPool(pool);
  Class.forName("org.apache.commons.dbcp2.PoolingDriver");
  driver = (PoolingDriver) DriverManager.getDriver(CONNECTIONS_DRIVER_URL);
  driver.registerPool(CONNECTIONS_POOL_NAME, pool);
}
/*line 93 "src/tex/0-Overview.nw"*/
private 
/*line 497 "src/tex/4-Administration.nw"*/
void JargoSetupPreparedStatements() {
  final String INS = "INSERT INTO ";
  final String UPD = "UPDATE ";
  final String DEL = "DELETE FROM ";
  final String SEL = "SELECT ";
  final String q2  = "(?,?)";
  final String q3  = "(?,?,?)";
  final String q4  = "(?,?,?,?)";
  final String q7  = "(?,?,?,?,?,?,?)";
  final String q8  = "(?,?,?,?,?,?,?,?)";
  final String q9  = "(?,?,?,?,?,?,?,?,?)";
  final String q12 = "(?,?,?,?,?,?,?,?,?,?,?,?)";
  final String q14 = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  
/*line 524 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S0", INS+"V VALUES "+q3);
/*line 510 "src/tex/4-Administration.nw"*/
         
/*line 527 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S1", INS+"E VALUES "+q4);
/*line 510 "src/tex/4-Administration.nw"*/
                
/*line 530 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S2", INS+"UQ VALUES "+q2);
/*line 510 "src/tex/4-Administration.nw"*/
                       
/*line 533 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S3", INS+"UE VALUES "+q2);
/*line 510 "src/tex/4-Administration.nw"*/
                              
/*line 536 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S4", INS+"UL VALUES "+q2);
/*line 510 "src/tex/4-Administration.nw"*/
                                     
/*line 539 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S5", INS+"UO VALUES "+q2);
/*line 510 "src/tex/4-Administration.nw"*/
                                            
/*line 542 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S6", INS+"UD VALUES "+q2);
/*line 510 "src/tex/4-Administration.nw"*/
                                                   
/*line 545 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S7", INS+"UB VALUES "+q2);
/*line 510 "src/tex/4-Administration.nw"*/
                                                          
/*line 548 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S8", INS+"S VALUES "+q7);
/*line 510 "src/tex/4-Administration.nw"*/
                                                                 
/*line 551 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S9", INS+"R VALUES "+q7);
/*line 510 "src/tex/4-Administration.nw"*/
                                                                        
/*line 554 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S10", INS+"W VALUES "+q8);
/*line 511 "src/tex/4-Administration.nw"*/
  
/*line 560 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S11", INS+"CW VALUES "+q9);
/*line 511 "src/tex/4-Administration.nw"*/
          
/*line 563 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S12", INS+"PD VALUES "+q4);
/*line 511 "src/tex/4-Administration.nw"*/
                  
/*line 566 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S13", INS+"CPD VALUES "+q12);
/*line 511 "src/tex/4-Administration.nw"*/
                          
/*line 569 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S14", INS+"CQ VALUES "+q14);
/*line 511 "src/tex/4-Administration.nw"*/
                                  
/*line 572 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S15", UPD+"E SET nu=? WHERE v1=? AND v2=?");
/*line 511 "src/tex/4-Administration.nw"*/
                                          
/*line 575 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S131", UPD+"W SET nu=? WHERE v1=? AND v2=?");
/*line 511 "src/tex/4-Administration.nw"*/
                                                   
/*line 578 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S77", UPD+"CW SET te=?, ve=? WHERE sid=?");
/*line 511 "src/tex/4-Administration.nw"*/
                                                           
/*line 581 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S84", UPD+"PD SET t2=? WHERE v2=? AND rid=?");
/*line 511 "src/tex/4-Administration.nw"*/
                                                                   
/*line 584 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S82", UPD+"CPD SET tp=? WHERE vp=? AND rid=?");
/*line 512 "src/tex/4-Administration.nw"*/
  
/*line 587 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S83", UPD+"CPD SET td=? WHERE vd=? AND rid=?");
/*line 512 "src/tex/4-Administration.nw"*/
          
/*line 590 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S76", DEL+"W WHERE sid=? AND t2>?");
/*line 512 "src/tex/4-Administration.nw"*/
                  
/*line 593 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S42", DEL+"PD WHERE rid=?");
/*line 512 "src/tex/4-Administration.nw"*/
                          
/*line 596 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S43", DEL+"CPD WHERE rid=?");
/*line 512 "src/tex/4-Administration.nw"*/
                                  
/*line 599 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S80", DEL+"CQ WHERE sid=? AND t2>?");
/*line 512 "src/tex/4-Administration.nw"*/
                                          
/*line 602 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S62", SEL+"COUNT (*) FROM V WHERE v<>0");
/*line 512 "src/tex/4-Administration.nw"*/
                                                  
/*line 605 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S64", SEL+"MIN (lng), MAX (lng), MIN (lat), MAX (lat) "
      + "FROM V WHERE v<>0");
/*line 512 "src/tex/4-Administration.nw"*/
                                                          
/*line 609 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S63", SEL+"COUNT (*) FROM E WHERE v1<>0 AND v2<>0");
/*line 512 "src/tex/4-Administration.nw"*/
                                                                  
/*line 612 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S65", SEL+"MIN (dd), MAX (dd), SUM (dd) / COUNT (dd), "
      + "MIN (nu), MAX (nu), SUM (nu) / COUNT (nu) "
      + "FROM E WHERE v1<>0 AND v2<>0");
/*line 513 "src/tex/4-Administration.nw"*/
  
/*line 617 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S46", SEL+"dd, nu FROM E WHERE v1=? AND v2=?");
/*line 513 "src/tex/4-Administration.nw"*/
          
/*line 620 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S130", SEL+"lng, lat FROM V WHERE v=?");
/*line 513 "src/tex/4-Administration.nw"*/
                   
/*line 557 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S70", SEL+"sid, sq, se, sl, so, sd, sb FROM S WHERE sid=?");
/*line 513 "src/tex/4-Administration.nw"*/
                           
/*line 623 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S48", SEL+"sq, se FROM S WHERE sid=?");
/*line 513 "src/tex/4-Administration.nw"*/
                                   
/*line 626 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S66", SEL+"COUNT (*) FROM S");
/*line 513 "src/tex/4-Administration.nw"*/
                                           
/*line 629 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S75", SEL+"rid, rq, re, rl, ro, rd, rb FROM R WHERE rid=?");
/*line 513 "src/tex/4-Administration.nw"*/
                                                   
/*line 632 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S51", SEL+"rq, re, rl, ro, rd FROM R WHERE rid=?");
/*line 513 "src/tex/4-Administration.nw"*/
                                                           
/*line 635 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S67", SEL+"COUNT (*) FROM R");
/*line 513 "src/tex/4-Administration.nw"*/
                                                                   
/*line 638 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S59", SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
      + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
      + "GROUP BY sid"
      + ") as b ON a.sid=b.sid AND ABS(a.t2-?)=b.tdiff AND a.t2<=?");
/*line 514 "src/tex/4-Administration.nw"*/
  
/*line 644 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S128", SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
      + "SELECT sid FROM CW WHERE te>? OR (ve=0 AND sl>?)"
      + ") as b ON a.sid=b.sid INNER JOIN ("
      + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
      + "GROUP BY sid"
      + ") as c ON a.sid=c.sid AND ABS(a.t2-?)=c.tdiff AND a.t2<=?");
/*line 514 "src/tex/4-Administration.nw"*/
           
/*line 652 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S60", SEL+"t, v FROM r_server WHERE sid=? ORDER BY t ASC");
/*line 514 "src/tex/4-Administration.nw"*/
                   
/*line 655 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S129", SEL+"t, v FROM r_server WHERE sid=? AND t>? ORDER BY t ASC");
/*line 514 "src/tex/4-Administration.nw"*/
                            
/*line 658 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S61", SEL+"t, v, Ls, Lr FROM r_server WHERE sid=?"
      + "AND (Ls IS NOT NULL OR Lr IS NOT NULL) ORDER BY t ASC");
/*line 514 "src/tex/4-Administration.nw"*/
                                    
/*line 664 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S69", SEL+"t, v, Ls, Lr "
      + "FROM r_server LEFT JOIN CQ ON t=t2 and v=v2 and Lr=rid "
      + "WHERE r_server.sid=?"
      + "   AND (t>? OR v=0)"
      + "   AND (Ls IS NOT NULL OR Lr IS NOT NULL)"
      + "ORDER BY t ASC, o2 ASC");
/*line 514 "src/tex/4-Administration.nw"*/
                                            
/*line 672 "src/tex/4-Administration.nw"*/
// A "timeout" of 30 seconds is hard-coded here
this.lu_pstr.put("S68", SEL+"* FROM R WHERE re<=? AND ?<=re+30 AND rid NOT IN  "
      + "(SELECT rid FROM assignments_r)");
/*line 514 "src/tex/4-Administration.nw"*/
                                                    
/*line 677 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S85", SEL+"uq FROM UQ WHERE uid=?");
/*line 514 "src/tex/4-Administration.nw"*/
                                                            
/*line 680 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S86", SEL+"tp, td FROM CPD WHERE rid=?");
/*line 514 "src/tex/4-Administration.nw"*/
                                                                    
/*line 683 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S73", SEL+"q2 FROM CQ WHERE sid=? AND t2<=? "
      + "ORDER BY t2 DESC, q2 DESC FETCH FIRST ROW ONLY");
/*line 515 "src/tex/4-Administration.nw"*/
  
/*line 687 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S87", SEL+"t2, q2, o2 FROM CQ WHERE sid=? AND t2<=? "
      + "ORDER BY t2 DESC, o2 DESC FETCH FIRST ROW ONLY");
/*line 515 "src/tex/4-Administration.nw"*/
          
/*line 691 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S100", SEL+"rid FROM assignments WHERE t>? AND sid=?");
/*line 515 "src/tex/4-Administration.nw"*/
                   
/*line 694 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S101", SEL+"rid FROM assignments WHERE t<=? AND sid=?");
/*line 515 "src/tex/4-Administration.nw"*/
                            
/*line 697 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S102", SEL+"* FROM service_rate");
/*line 515 "src/tex/4-Administration.nw"*/
                                     
/*line 700 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S103", SEL+"* FROM dist_base");
/*line 515 "src/tex/4-Administration.nw"*/
                                              
/*line 703 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S104", SEL+"val FROM dist_s_travel WHERE sid=?");
/*line 515 "src/tex/4-Administration.nw"*/
                                                       
/*line 706 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S105", SEL+"SUM (val) FROM dist_s_travel");
/*line 515 "src/tex/4-Administration.nw"*/
                                                                
/*line 709 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S106", SEL+"val FROM dist_s_cruising WHERE sid=?");
/*line 516 "src/tex/4-Administration.nw"*/
  
/*line 712 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S107", SEL+"SUM (val) FROM dist_s_cruising");
/*line 516 "src/tex/4-Administration.nw"*/
           
/*line 715 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S108", SEL+"val FROM dist_s_service WHERE sid=?");
/*line 516 "src/tex/4-Administration.nw"*/
                    
/*line 718 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S109", SEL+"SUM (val) FROM dist_s_service");
/*line 516 "src/tex/4-Administration.nw"*/
                             
/*line 721 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S110", SEL+"val FROM dist_s_base");
/*line 516 "src/tex/4-Administration.nw"*/
                                      
/*line 724 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S111", SEL+"val FROM dist_r_base");
/*line 516 "src/tex/4-Administration.nw"*/
                                               
/*line 727 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S112", SEL+"val FROM dist_r_detour WHERE rid=?");
/*line 516 "src/tex/4-Administration.nw"*/
                                                        
/*line 730 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S113", SEL+"SUM (val) FROM dist_r_detour");
/*line 516 "src/tex/4-Administration.nw"*/
                                                                 
/*line 733 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S114", SEL+"val FROM dist_r_transit WHERE rid=?");
/*line 517 "src/tex/4-Administration.nw"*/
  
/*line 736 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S115", SEL+"SUM (val) FROM dist_r_transit");
/*line 517 "src/tex/4-Administration.nw"*/
           
/*line 739 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S116", SEL+"val FROM dur_s_travel WHERE sid=?");
/*line 517 "src/tex/4-Administration.nw"*/
                    
/*line 742 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S117", SEL+"SUM (val) FROM dur_s_travel");
/*line 517 "src/tex/4-Administration.nw"*/
                             
/*line 745 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S118", SEL+"val FROM dur_r_pickup WHERE rid=?");
/*line 517 "src/tex/4-Administration.nw"*/
                                      
/*line 748 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S119", SEL+"SUM (val) FROM dur_r_pickup");
/*line 517 "src/tex/4-Administration.nw"*/
                                               
/*line 751 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S120", SEL+"val FROM dur_r_transit WHERE rid=?");
/*line 517 "src/tex/4-Administration.nw"*/
                                                        
/*line 754 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S121", SEL+"SUM (val) FROM dur_r_transit");
/*line 517 "src/tex/4-Administration.nw"*/
                                                                 
/*line 757 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S122", SEL+"val FROM dur_r_travel WHERE rid=?");
/*line 518 "src/tex/4-Administration.nw"*/
  
/*line 760 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S123", SEL+"SUM (val) FROM dur_r_travel");
/*line 518 "src/tex/4-Administration.nw"*/
           
/*line 763 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S124", SEL+"val FROM t_r_depart WHERE rid=?");
/*line 518 "src/tex/4-Administration.nw"*/
                    
/*line 766 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S125", SEL+"val FROM t_s_depart WHERE sid=?");
/*line 518 "src/tex/4-Administration.nw"*/
                             
/*line 769 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S126", SEL+"val FROM t_r_arrive WHERE rid=?");
/*line 518 "src/tex/4-Administration.nw"*/
                                      
/*line 772 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S127", SEL+"val FROM t_s_arrive WHERE sid=?");
/*line 518 "src/tex/4-Administration.nw"*/
                                               
/*line 775 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S133", SEL+"val FROM f_status WHERE rid=? AND t<=? "
    + "ORDER BY t DESC FETCH FIRST ROW ONLY");
/*line 518 "src/tex/4-Administration.nw"*/
                                                        
/*line 779 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S134", SEL+"sid, te FROM CW WHERE se<=? AND (?<te OR (ve=0 AND sl>?))");
/*line 518 "src/tex/4-Administration.nw"*/
                                                                 
/*line 782 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S135", SEL+"t2, v2 FROM W WHERE sid=? AND t2=("
    + "SELECT t1 FROM W WHERE sid=? AND ? <= t1 AND t1 <= ? AND ? < t2)");
/*line 519 "src/tex/4-Administration.nw"*/
  
/*line 786 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S136", SEL+"* FROM V");
/*line 519 "src/tex/4-Administration.nw"*/
           
/*line 789 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S137", SEL+"* FROM E");
/*line 519 "src/tex/4-Administration.nw"*/
                    
/*line 792 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S138", SEL+"val FROM dist_r_unassigned");
/*line 519 "src/tex/4-Administration.nw"*/
                             
/*line 795 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S139", UPD+"CPD SET te=? WHERE sid=?");
/*line 519 "src/tex/4-Administration.nw"*/
                                      
/*line 798 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S140", UPD+"CQ SET tp=?, td=? WHERE rid=?");
/*line 519 "src/tex/4-Administration.nw"*/
                                               
/*line 801 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S141", SEL+"* FROM r_user");
/*line 519 "src/tex/4-Administration.nw"*/
                                                        
/*line 804 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S142", SEL+"SUM (dd) FROM W WHERE sid=? AND t2>?");
/*line 519 "src/tex/4-Administration.nw"*/
                                                                 
/*line 807 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S143", SEL+"* FROM R WHERE re<=? AND ?<=re+?");
/*line 520 "src/tex/4-Administration.nw"*/
  
/*line 810 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S144", SEL+"t2, v2, rid FROM CQ WHERE sid=? AND t2>? ORDER BY o2 ASC");
/*line 520 "src/tex/4-Administration.nw"*/
           
/*line 813 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S145", SEL+"te, ve FROM CW WHERE sid=?");
/*line 520 "src/tex/4-Administration.nw"*/
                    
/*line 816 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S147", SEL+"t2, v2 FROM W WHERE sid=? AND t2=("
    + "SELECT t1 FROM W WHERE sid=? AND v2=0)");
/*line 520 "src/tex/4-Administration.nw"*/
                             
/*line 820 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S148", SEL+"1 FROM assignments_r WHERE rid=?");
/*line 520 "src/tex/4-Administration.nw"*/
                                      
/*line 823 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S149", SEL+"t2, v2 FROM W WHERE sid=? ORDER BY t2 ASC");
/*line 520 "src/tex/4-Administration.nw"*/
                                               
/*line 826 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S150", SEL+"sid, val FROM violations_t_s");
/*line 520 "src/tex/4-Administration.nw"*/
                                                        
/*line 829 "src/tex/4-Administration.nw"*/
this.lu_pstr.put("S151", SEL+"rid, val FROM violations_t_r");
/*line 521 "src/tex/4-Administration.nw"*/
}
/*line 94 "src/tex/0-Overview.nw"*/
private 
/*line 885 "src/tex/4-Administration.nw"*/
void PSAdd(PreparedStatement p, final Integer... values) throws SQLException {
  p.clearParameters();
  
/*line 13 "src/tex/4-Administration.nw"*/
for (int i = 0; i < values.length; i++) {
  if (values[i] == null) {
    p.setNull((i + 1), java.sql.Types.INTEGER);
  } else {
    p.setInt ((i + 1), values[i]);
  }
}
/*line 888 "src/tex/4-Administration.nw"*/
  try {
    p.addBatch();
  } catch (SQLException e) {
    throw e;
  }
}
/*line 95 "src/tex/0-Overview.nw"*/
private 
/*line 852 "src/tex/4-Administration.nw"*/
PreparedStatement PSCreate(final Connection conn, final String k) throws SQLException {
  PreparedStatement p = null;
  try {
    p = conn.prepareStatement(lu_pstr.get(k),
      ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    p.clearBatch();
    p.clearParameters();
  } catch (SQLException e) {
    throw e;
  }
  return p;
}
/*line 96 "src/tex/0-Overview.nw"*/
private 
/*line 959 "src/tex/4-Administration.nw"*/
int[] PSQuery(final Connection conn, final String k, final int ncols, final Integer... values)
throws SQLException {
  int[] output = new int[] { };
  try {
    PreparedStatement p = PSCreate(conn, k);
    
/*line 13 "src/tex/4-Administration.nw"*/
for (int i = 0; i < values.length; i++) {
  if (values[i] == null) {
    p.setNull((i + 1), java.sql.Types.INTEGER);
  } else {
    p.setInt ((i + 1), values[i]);
  }
}
/*line 965 "src/tex/4-Administration.nw"*/
    ResultSet res = p.executeQuery();
    if (res.last() == true) {
      
/*line 8 "src/tex/2-Reading.nw"*/
output = new int[(ncols*res.getRow())];
res.first();
do {
  for (int j = 1; j <= ncols; j++) {
    output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
  }
} while (res.next());
/*line 968 "src/tex/4-Administration.nw"*/
    }
    res.close();
    p.close();
  } catch (SQLException e) {
    throw e;
  }
  return output;
}
/*line 97 "src/tex/0-Overview.nw"*/
private 
/*line 913 "src/tex/4-Administration.nw"*/
void PSSubmit(PreparedStatement... statements) throws SQLException {
  try {
    for (PreparedStatement p : statements) {
      p.executeBatch();
      p.close();
    }
  } catch (SQLException e) {
    throw e;
  }
}
/*line 10 "src/Storage.nw"*/
}
