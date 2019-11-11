package com.github.jargors;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import java.sql.CallableStatement;   import java.sql.Connection;
import java.sql.DriverManager;       import java.sql.PreparedStatement;
import java.sql.ResultSet;           import java.sql.SQLException;
import java.sql.Statement;           import java.sql.Types;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public class Storage {
  private Map<Integer, Boolean> lu_rstatus = new HashMap<>();  //*
  private ConcurrentHashMap<String, String> lu_pstr     = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, int[]> lu_vertices = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>>    lu_edges    = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, int[]> lu_users    = new ConcurrentHashMap<>();
  private final int    STATEMENTS_MAX_COUNT   = 20;
  private final int    REQUEST_TIMEOUT        = 30;
  private       String CONNECTIONS_URL        = "jdbc:derby:memory:jargo;create=true";
  private final String CONNECTIONS_DRIVER_URL = "jdbc:apache:commons:dbcp:";
  private final String CONNECTIONS_POOL_NAME  = "jargo";
  private final String CONNECTIONS_POOL_URL   = (CONNECTIONS_DRIVER_URL + CONNECTIONS_POOL_NAME);
  private ConnectionFactory               connection_factory;
  private PoolableConnectionFactory       poolableconnection_factory;
  private ObjectPool<PoolableConnection>  pool;
  private PoolingDriver                   driver;
  public Storage() {
    this.PSInit();
  }
  public int[] DBQuery(final String sql, final int ncols) throws SQLException {
    int[] output = new int[] { };
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      Statement stmt = conn.createStatement(
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet res = stmt.executeQuery(sql);
      if (res.last()) {
        output = new int[(ncols*res.getRow())];
        res.first();
        do {
          for (int j = 1; j <= ncols; j++) {
            output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
          }
        } while (res.next());
      }
      conn.close();
    } catch (SQLException e) {
      throw e;
    }
    return output;
  }
  public int[] DBQueryCountVertices() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S62", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryCountEdges() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S63", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryVertex(final int v) throws VertexNotFoundException {
    if (!this.lu_vertices.containsKey(v)) {
      throw new VertexNotFoundException("Vertex "+v+" not found.");
    }
    return this.lu_vertices.get(v).clone();
  }
  public int[] DBQueryAllVertices() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S136", 3);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryEdge(final int v1, final int v2) throws EdgeNotFoundException {
    if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
      throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
    }
    return this.lu_edges.get(v1).get(v2).clone();
  }
  public int[] DBQueryAllEdges() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S137", 4);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryStatisticsEdges() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S65", 6);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryMBR() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S64", 4);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryCountServers() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S66", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryCountRequests() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S67", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryAllUsers() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S141", 7);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryUser(final int uid)
  throws UserNotFoundException {
    if (!this.lu_users.containsKey(uid)) {
      throw new UserNotFoundException("User "+uid+" not found.");
    }
    return this.lu_users.get(uid).clone();
  }
  public int[] DBQueryRequestStatus(final int rid, final int t)
  throws UserNotFoundException, SQLException {
    if (!this.lu_users.containsKey(rid)) {
      throw new UserNotFoundException("User "+rid+" not found.");
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S133", 1, rid, t);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestIsAssigned(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S148", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryQueuedRequests(final int t) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      final int[] output = this.DBFetch(conn, "S143", 7, t, t, REQUEST_TIMEOUT);
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
  public int[] DBQueryActiveServers(final int t) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S134", 1, t, t, t);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerLocationsAll(final int t) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return this.DBFetch(conn, "S59", 3, t, t, t, t);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerLocationsActive(final int t) throws SQLException {
    int[] output = new int[] { };
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      int j = 0;
      final int[] temp1 = this.DBFetch(conn, "S134", 2, t, t, t);  // <-- 10 ms/call
      output = new int[(3*(temp1.length/2))];
      for (int i = 0; i < temp1.length - 1; i += 2) {
        final int sid = temp1[(i + 0)];
        final int  te = temp1[(i + 1)];
        final int[] temp2 = (t < te
          ? this.DBFetch(conn, "S135", 2, sid, sid, t, t)  // <-- 0.07-0.15 ms/call
          : this.DBFetch(conn, "S147", 2, sid, sid));      // <-- 0.04-0.15 ms/call
        output[(j + 0)] = sid;
        output[(j + 1)] = temp2[0];
        output[(j + 2)] = temp2[1];
        j += 3;
      }
    } catch (SQLException e) {
      throw e;
    }
    return output;
  }
  public int[] DBQueryServerRoute(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S60", 2, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerSchedule(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S61", 4, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerRemainingRoute(final int sid, final int t) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S129", 2, sid, t);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerRemainingSchedule(final int sid, final int t)
  throws SQLException {
    int[] output = new int[] { };
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      int[] temp = DBFetch(conn, "S144", 3, sid, t);
      output = new int[(4*temp.length/3 + 4)];
      int j = 0;
      for (int i = 0; i < (temp.length - 2); i += 3) {
        output[(j + 0)] = temp[(i + 0)];
        output[(j + 1)] = temp[(i + 1)];
        output[(j + 2)] = 0;
        output[(j + 3)] = temp[(i + 2)];
        j += 4;
      }
      temp = DBFetch(conn, "S145", 2, sid);
      output[(j + 0)] = temp[0];
      output[(j + 1)] = temp[1];
      output[(j + 2)] = sid;
      output[(j + 3)] = 0;
    } catch (SQLException e) {
      throw e;
    }
    return output;
  }
  public int[] DBQueryServerRemainingDistance(final int sid, final int t)
  throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S142", 1, sid, t);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerRemainingDuration(final int sid, final int t)
  throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      int[] output = DBFetch(conn, "S127", 1, sid, t);
      if (output != null) {
        output[0] -= t;
      }
      return output;
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerMaxLoad(final int sid, final int t) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S73", 1, sid, t);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerPendingAssignments(final int sid, final int t)
  throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S100", 1, t, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerCompletedAssignments(final int sid, final int t)
  throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S101", 1, t, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServiceRate() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S102", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryBaseDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S103", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerBaseDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S110", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestBaseDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S111", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestBaseDistanceUnassigned() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S138", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerTravelDistance(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S104", 1, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerTravelDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S105", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerCruisingDistance(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S106", 1, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerCruisingDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S107", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerServiceDistance(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S108", 1, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerServiceDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S109", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestDetourDistance(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S112", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestDetourDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S113", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestTransitDistance(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S114", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestTransitDistanceTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S115", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerTravelDuration(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S116", 1, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerTravelDurationTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S117", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestPickupDuration(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S118", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestPickupDurationTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S119", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestTransitDuration(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S120", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestTransitDurationTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S121", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestTravelDuration(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S122", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestTravelDurationTotal() throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S123", 1);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestDepartureTime(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S124", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerDepartureTime(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S125", 1, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryRequestArrivalTime(final int rid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S126", 1, rid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public int[] DBQueryServerArrivalTime(final int sid) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      return DBFetch(conn, "S127", 1, sid);
    } catch (SQLException e) {
      throw e;
    }
  }
  public void DBAddNewVertex(final int v, final int lng, final int lat)
  throws DuplicateVertexException, SQLException {
    if (this.lu_vertices.containsKey(v)) {
      throw new DuplicateVertexException("Vertex "+v+" already exists.");
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
        PreparedStatement pS0 = this.PS(conn, "S0");
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
  public void DBAddNewEdge(final int v1, final int v2, final int dd, final int nu)
  throws DuplicateEdgeException, SQLException {
    if (this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2)) {
      throw new DuplicateEdgeException("Edge ("+v1+", "+v2+") already exists.");
    }
    if (!this.lu_edges.containsKey(v1)) {
      this.lu_edges.put(v1, new ConcurrentHashMap());
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
        PreparedStatement pS1 = this.PS(conn, "S1");
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
  public void DBAddNewRequest(final int[] u)
  throws DuplicateUserException, SQLException {
    final int uid = u[0];
    if (this.lu_users.containsKey(uid)) {
      throw new DuplicateUserException("User "+uid+" already exists.");
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
        PreparedStatement pS2 = this.PS(conn, "S2");
        PreparedStatement pS3 = this.PS(conn, "S3");
        PreparedStatement pS4 = this.PS(conn, "S4");
        PreparedStatement pS5 = this.PS(conn, "S5");
        PreparedStatement pS6 = this.PS(conn, "S6");
        PreparedStatement pS7 = this.PS(conn, "S7");
        this.PSAdd(pS2, uid, u[1]);
        this.PSAdd(pS3, uid, u[2]);
        this.PSAdd(pS4, uid, u[3]);
        this.PSAdd(pS5, uid, u[4]);
        this.PSAdd(pS6, uid, u[5]);
        this.PSAdd(pS7, uid, u[6]);
        this.PSSubmit(pS2, pS3, pS4, pS5, pS6, pS7);
        PreparedStatement pS9 = this.PS(conn, "S9");
        this.PSAdd(pS9, uid, u[1], u[2], u[3], u[4], u[5], u[6]);
        this.PSSubmit(pS9);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw e;
    }
    this.lu_users.put(u[0], u.clone());
    this.lu_rstatus.put(u[0], false);
  }
  public void DBAddNewServer(final int[] u, final int[] route)
  throws DuplicateUserException, EdgeNotFoundException, SQLException {
    final int uid = u[0];
    if (this.lu_users.containsKey(uid)) {
      throw new DuplicateUserException("User "+uid+" already exists.");
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
        final int se = u[2];
        PreparedStatement pS2 = this.PS(conn, "S2");
        PreparedStatement pS3 = this.PS(conn, "S3");
        PreparedStatement pS4 = this.PS(conn, "S4");
        PreparedStatement pS5 = this.PS(conn, "S5");
        PreparedStatement pS6 = this.PS(conn, "S6");
        PreparedStatement pS7 = this.PS(conn, "S7");
        this.PSAdd(pS2, uid, u[1]);
        this.PSAdd(pS3, uid, u[2]);
        this.PSAdd(pS4, uid, u[3]);
        this.PSAdd(pS5, uid, u[4]);
        this.PSAdd(pS6, uid, u[5]);
        this.PSAdd(pS7, uid, u[6]);
        this.PSSubmit(pS2, pS3, pS4, pS5, pS6, pS7);
  /*L1*/PreparedStatement pS8 = this.PS(conn, "S8");
        this.PSAdd(pS8, uid, u[1], u[2], u[3], u[4], u[5], u[6]);
        this.PSSubmit(pS8);
  /*L2*/PreparedStatement pS10 = this.PS(conn, "S10");
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
        pS10 = this.PS(conn, "S10");
        this.PSAdd(pS10, uid, se, null, null, route[0], route[1], null, null);
        this.PSSubmit(pS10);
  /*L3*/PreparedStatement pS11 = this.PS(conn, "S11");
        final int te = route[(route.length - 2)];
        this.PSAdd(pS11, uid, u[2], u[3], u[4], u[5], u[2], u[4], te, u[5]);
        this.PSSubmit(pS11);
  /*L4*/PreparedStatement pS14 = this.PS(conn, "S14");
        this.PSAdd(pS14, uid, u[1], u[2], null, u[2], u[4], null, u[1],
            null, null, null, null, null, 1);
        this.PSSubmit(pS14);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw e;
    }
    this.lu_users.put(uid, u.clone());
  }
  public void DBUpdateEdgeSpeed(final int v1, final int v2, final int nu)
  throws EdgeNotFoundException, SQLException {
    if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
      throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
        PreparedStatement pS15 = this.PS(conn, "S15");
        PreparedStatement pS131 = this.PS(conn, "S131");
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
  public void DBUpdateServerRoute(final int sid, final int[] route, final int[] sched)
  throws UserNotFoundException, EdgeNotFoundException, SQLException {
    if (!this.lu_users.containsKey(sid)) {
      throw new UserNotFoundException("User "+sid+" not found.");
    }
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
  /*L1*/final int sq = lu_users.get(sid)[1];
        final int se = lu_users.get(sid)[2];
  /*L2*//*a*/PreparedStatement pS76 = this.PS(conn, "S76");
             this.PSAdd(pS76, sid, route[0]);
             this.PSSubmit(pS76);
        /*b*/final int uid = sid;
             PreparedStatement pS10 = this.PS(conn, "S10");
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
        /*c*/PreparedStatement pS77 = this.PS(conn, "S77");
             PreparedStatement pS139 = this.PS(conn, "S139");
             final int te = sched[(sched.length - 3)];
             final int ve = sched[(sched.length - 2)];
             this.PSAdd(pS77, te, ve, sid);
             this.PSAdd(pS139, te, sid);
             this.PSSubmit(pS77, pS139);
        if (sched.length > 0) {
          Map<Integer, int[]> cache = new HashMap<>();
  /*L3*/  /*a*/PreparedStatement pS82 = this.PS(conn, "S82");
               PreparedStatement pS83 = this.PS(conn, "S83");
               PreparedStatement pS84 = this.PS(conn, "S84");
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
          /*b*/PreparedStatement pS140 = this.PS(conn, "S140");
               for (int j = 0; j < (sched.length - 2); j += 3) {
                 final int Lj = sched[(j + 2)];
                 if (Lj != sid) {
                   if (!cache.containsKey(Lj)) {
                     final int[] output = DBFetch(conn, "S86", 2, Lj);
                     final int tp = output[0];
                     final int td = output[1];
                     final int rq = this.lu_users.get(Lj)[1];
                     cache.put(Lj, new int[] { rq, tp, td });
                     this.PSAdd(pS140, tp, td, Lj);
                   }
                 }
               }
               this.PSSubmit(pS140);
          /*c*/final int[] output = (route[0] == 0 ? null : this.DBFetch(conn, "S87", 3, sid, route[0]));
               int t1 = (route[0] == 0 ?  0 : output[0]);
               int q1 = (route[0] == 0 ? sq : output[1]);
               int o1 = (route[0] == 0 ?  1 : output[2]);
          /*d*/PreparedStatement pS80 = this.PS(conn, "S80");
               this.PSAdd(pS80, sid, route[0]);
               this.PSSubmit(pS80);
          /*e*/PreparedStatement pS14 = PS(conn, "S14");
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
  public void DBUpdateServerAddToSchedule(
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
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
  /*L1*/final int sq = lu_users.get(sid)[1];
        final int se = lu_users.get(sid)[2];
  /*L2*//*a*/PreparedStatement pS76 = this.PS(conn, "S76");
             this.PSAdd(pS76, sid, route[0]);
             this.PSSubmit(pS76);
        /*b*/final int uid = sid;
             PreparedStatement pS10 = this.PS(conn, "S10");
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
        /*c*/PreparedStatement pS77 = this.PS(conn, "S77");
             PreparedStatement pS139 = this.PS(conn, "S139");
             final int te = sched[(sched.length - 3)];
             final int ve = sched[(sched.length - 2)];
             this.PSAdd(pS77, te, ve, sid);
             this.PSAdd(pS139, te, sid);
             this.PSSubmit(pS77, pS139);
  /*L3*//*a*/PreparedStatement pS82 = this.PS(conn, "S82");
             PreparedStatement pS83 = this.PS(conn, "S83");
             PreparedStatement pS84 = this.PS(conn, "S84");
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
        /*b*/PreparedStatement pS140 = this.PS(conn, "S140");
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
                   final int[] output = this.DBFetch(conn, "S86", 2, Lj);
                   final int tp = output[0];
                   final int td = output[1];
                   this.PSAdd(pS140, tp, td, Lj);
                   cache.put(Lj, new int[] { rq, tp, td });
                 }
               }
             }
             this.PSSubmit(pS140);
        /*c*/final int[] output = (route[0] == 0 ? null : this.DBFetch(conn, "S87", 3, sid, route[0]));
             int t1 = (route[0] == 0 ?  0 : output[0]);
             int q1 = (route[0] == 0 ? sq : output[1]);
             int o1 = (route[0] == 0 ?  1 : output[2]);
        /*d*/PreparedStatement pS80 = this.PS(conn, "S80");
             this.PSAdd(pS80, sid, route[0]);
             this.PSSubmit(pS80);
        /*e*/PreparedStatement pS14 = PS(conn, "S14");
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
        /*f*/PreparedStatement pS12 = this.PS(conn, "S12");
             PreparedStatement pS13 = this.PS(conn, "S13");
             for (final int r : rid) {
               final int[] output2 = this.DBFetch(conn, "S51", 5, r);
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
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw e;
    }
    for (final int r : rid) {
      this.lu_rstatus.put(r, true);
    }
  }
  public void DBUpdateServerRemoveFromSchedule(
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
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      try {
  /*L1*/final int sq = lu_users.get(sid)[1];
        final int se = lu_users.get(sid)[2];
  /*L2*//*a*/PreparedStatement pS76 = this.PS(conn, "S76");
             this.PSAdd(pS76, sid, route[0]);
             this.PSSubmit(pS76);
        /*b*/final int uid = sid;
             PreparedStatement pS10 = this.PS(conn, "S10");
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
        /*c*/PreparedStatement pS77 = this.PS(conn, "S77");
             PreparedStatement pS139 = this.PS(conn, "S139");
             final int te = sched[(sched.length - 3)];
             final int ve = sched[(sched.length - 2)];
             this.PSAdd(pS77, te, ve, sid);
             this.PSAdd(pS139, te, sid);
             this.PSSubmit(pS77, pS139);
  /*L3*//*a*/PreparedStatement pS82 = this.PS(conn, "S82");
             PreparedStatement pS83 = this.PS(conn, "S83");
             PreparedStatement pS84 = this.PS(conn, "S84");
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
        /*b*/PreparedStatement pS140 = this.PS(conn, "S140");
             for (int j = 0; j < (sched.length - 2); j += 3) {
               final int Lj = sched[(j + 2)];
               if (Lj != sid) {
                 if (!cache.containsKey(Lj)) {
                   final int[] output = DBFetch(conn, "S86", 2, Lj);
                   final int tp = output[0];
                   final int td = output[1];
                   final int rq = this.lu_users.get(Lj)[1];
                   cache.put(Lj, new int[] { rq, tp, td });
                   this.PSAdd(pS140, tp, td, Lj);
                 }
               }
             }
             this.PSSubmit(pS140);
        /*c*/final int[] output = (route[0] == 0 ? null : this.DBFetch(conn, "S87", 3, sid, route[0]));
             int t1 = (route[0] == 0 ?  0 : output[0]);
             int q1 = (route[0] == 0 ? sq : output[1]);
             int o1 = (route[0] == 0 ?  1 : output[2]);
        /*d*/PreparedStatement pS80 = this.PS(conn, "S80");
             this.PSAdd(pS80, sid, route[0]);
             this.PSSubmit(pS80);
        /*e*/PreparedStatement pS14 = PS(conn, "S14");
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
  /*L4*/PreparedStatement pS42 = this.PS(conn, "S42");
        PreparedStatement pS43 = this.PS(conn, "S43");
        for (final int r : rid) {
          this.PSAdd(pS42, r);
          this.PSAdd(pS43, r);
        }
        this.PSSubmit(pS42, pS43);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw e;
    }
    for (final int r : rid) {
      this.lu_rstatus.put(r, false);
    }
  }
  public void DBCreateNewInstance() throws SQLException {
    try {
      this.setupDriver();
    } catch (SQLException e) {
      throw e;
    } catch (ClassNotFoundException e) {
      System.err.println("Storage.init(): "+"Encountered catastrophic exception.");
      e.printStackTrace();
      System.exit(1);
    }
  }
  public void DBCloseInstance() throws SQLException {
    try {
      DriverManager.getConnection("jdbc:derby:memory:jargo;drop=true");
    } catch (SQLException e) {
      if (e.getErrorCode() != 45000) {
        throw e;
      }
    }
  }
  public final ConcurrentHashMap<Integer, int[]> getReferenceVerticesCache() {
    return this.lu_vertices;
  }
  public final ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>> getReferenceEdgesCache() {
    return this.lu_edges;
  }
  public final ConcurrentHashMap<Integer, int[]> getReferenceUsersCache() {
    return this.lu_users;
  }
  public void DBLoadDataModel() {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      Statement stmt = conn.createStatement();
      stmt.clearBatch();
      stmt.addBatch("CREATE TABLE V ("
                      + "v   int  CONSTRAINT P1 PRIMARY KEY,"
                      + "lng int  CONSTRAINT C1 NOT NULL,"
                      + "lat int  CONSTRAINT C2 NOT NULL,"
                      + "CONSTRAINT C3 CHECK (lng BETWEEN -1800000000 AND 1800000000),"
                      + "CONSTRAINT C4 CHECK (lat BETWEEN  -900000000 AND  900000000)"
                      + ")");
      stmt.addBatch("CREATE TABLE E ("
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
                      + ")");
      stmt.addBatch("CREATE TABLE UQ ("
                      + "uid int  CONSTRAINT C12 NOT NULL,"
                      + "uq  int  CONSTRAINT C13 NOT NULL,"
                      + "CONSTRAINT C14 UNIQUE (uid),"
                      + "CONSTRAINT C15 CHECK (uq != 0),"
                      + "CONSTRAINT P3 PRIMARY KEY (uid, uq)"
                      + ")");
      stmt.addBatch("CREATE TABLE UE ("
                      + "uid int  CONSTRAINT C16 NOT NULL,"
                      + "ue  int  CONSTRAINT C17 NOT NULL,"
                      + "CONSTRAINT C18 CHECK (ue BETWEEN 0 AND 86400000),"
                      + "CONSTRAINT C19 UNIQUE (uid),"
                      + "CONSTRAINT P4 PRIMARY KEY (uid, ue)"
                      + ")");
      stmt.addBatch("CREATE TABLE UL ("
                      + "uid int  CONSTRAINT C20 NOT NULL,"
                      + "ul  int  CONSTRAINT C21 NOT NULL,"
                      + "CONSTRAINT C22 UNIQUE (uid),"
                      + "CONSTRAINT C23 CHECK (ul BETWEEN 0 AND 86400000),"
                      + "CONSTRAINT P5 PRIMARY KEY (uid, ul)"
                      + ")");
      stmt.addBatch("CREATE TABLE UO ("
                      + "uid int  CONSTRAINT C24 NOT NULL,"
                      + "uo  int  CONSTRAINT C25 NOT NULL,"
                      + "CONSTRAINT F3 FOREIGN KEY (uo) REFERENCES V (v),"
                      + "CONSTRAINT C26 UNIQUE (uid),"
                      + "CONSTRAINT P6 PRIMARY KEY (uid, uo)"
                      + ")");
      stmt.addBatch("CREATE TABLE UD ("
                      + "uid int  CONSTRAINT C27 NOT NULL,"
                      + "ud  int  CONSTRAINT C28 NOT NULL,"
                      + "CONSTRAINT F4 FOREIGN KEY (ud) REFERENCES V (v),"
                      + "CONSTRAINT C29 UNIQUE (uid),"
                      + "CONSTRAINT P7 PRIMARY KEY (uid, ud)"
                      + ")");
      stmt.addBatch("CREATE TABLE UB ("
                      + "uid int  CONSTRAINT C30 NOT NULL,"
                      + "ub  int  CONSTRAINT C31 NOT NULL,"
                      + "CONSTRAINT C32 CHECK (ub >= 0),"
                      + "CONSTRAINT C33 UNIQUE (uid),"
                      + "CONSTRAINT P8 PRIMARY KEY (uid, ub)"
                      + ")");
      stmt.addBatch("CREATE TABLE S ("
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
                      + ")");
      stmt.addBatch("CREATE TABLE R ("
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
                      + ")");
      stmt.addBatch("CREATE TABLE W ("
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
                      + ")");
      stmt.addBatch("CREATE TABLE PD ("
                      + "sid int  CONSTRAINT C57 NOT NULL,"
                      + "t2  int  CONSTRAINT C58 NOT NULL,"
                      + "v2  int  CONSTRAINT C59 NOT NULL,"
                      + "rid int  CONSTRAINT C60 NOT NULL,"
                      + "CONSTRAINT P12 PRIMARY KEY (sid, t2, v2, rid),"
                      + "CONSTRAINT F21 FOREIGN KEY (sid) REFERENCES S,"
                      + "CONSTRAINT F22 FOREIGN KEY (rid) REFERENCES R,"
                      + "CONSTRAINT F23 FOREIGN KEY (sid, t2, v2) REFERENCES W INITIALLY DEFERRED"
                      + ")");
      stmt.addBatch("CREATE TABLE CW ("
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
                      + "CONSTRAINT C71 CHECK (ts = se AND vs = so),"
                      + "CONSTRAINT C72 CHECK (te <= sl AND ve = sd),"
                      + "CONSTRAINT C73 CHECK (ts < te)"
                      + ")");
      stmt.addBatch("CREATE TABLE CPD ("
                      + "sid int  CONSTRAINT C74 NOT NULL,"
                      + "ts  int  CONSTRAINT C75 NOT NULL,"
                      + "te  int  CONSTRAINT C76 NOT NULL,"
                      + "tp  int  CONSTRAINT C77 NOT NULL,"
                      + "vp  int  CONSTRAINT C78 NOT NULL,"
                      + "td  int  CONSTRAINT C79 NOT NULL,"
                      + "vd  int  CONSTRAINT C80 NOT NULL,"
                      + "rid int  CONSTRAINT C81 NOT NULL,"
                      + "re  int  CONSTRAINT C82 NOT NULL,"
                      + "rl  int  CONSTRAINT C83 NOT NULL,"
                      + "ro  int  CONSTRAINT C84 NOT NULL,"
                      + "rd  int  CONSTRAINT C85 NOT NULL,"
                      + "CONSTRAINT C86 UNIQUE (rid),"
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
                      + "CONSTRAINT C87 CHECK (tp BETWEEN ts AND td) INITIALLY DEFERRED,"
                      + "CONSTRAINT C88 CHECK (td BETWEEN tp AND te) INITIALLY DEFERRED,"
                      + "CONSTRAINT C89 CHECK (tp >= re AND vp = ro) INITIALLY DEFERRED,"
                      + "CONSTRAINT C90 CHECK (td <= rl AND vd = rd) INITIALLY DEFERRED"
                      + ")");
      stmt.addBatch("CREATE TABLE CQ ("
                      + "sid int  CONSTRAINT C91 NOT NULL,"
                      + "sq  int  CONSTRAINT C92 NOT NULL,"
                      + "se  int  CONSTRAINT C93 NOT NULL,"
                      + "t1  int  ,"
                      + "t2  int  CONSTRAINT C94 NOT NULL,"
                      + "v2  int  ,"
                      + "q1  int  ,"
                      + "q2  int  CONSTRAINT C95 NOT NULL,"
                      + "rid int  ,"
                      + "rq  int  ,"
                      + "tp  int  ,"
                      + "td  int  ,"
                      + "o1  int  ,"
                      + "o2  int  CONSTRAINT C96 NOT NULL,"
                      + "CONSTRAINT C97 CHECK (o2 > 0),"
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
                      + "CONSTRAINT C98 CHECK ("
                      + "  CASE WHEN t1 IS NULL"
                      + "    THEN t2 = se AND q1 IS NULL AND q2 = sq AND o1 IS NULL AND o2 = 1"
                      + "        AND rid IS NULL AND rq IS NULL AND tp IS NULL AND td IS NULL"
                      + "    ELSE q2 <= 0 AND o2 = o1 + 1"
                      + "  END"
                      + ") INITIALLY DEFERRED,"
                      + "CONSTRAINT  C99 CHECK (CASE WHEN t2 = tp THEN q2 = q1 + rq END) INITIALLY DEFERRED,"
                      + "CONSTRAINT C100 CHECK (CASE WHEN t2 = td THEN q2 = q1 - rq END) INITIALLY DEFERRED,"
                      + "CONSTRAINT C101 UNIQUE (t2, v2, rid)"
                      + ")");
      stmt.addBatch("CREATE VIEW r_user (uid, uq, ue, ul, uo, ud, ub) AS "
                      + "SELECT * from S UNION SELECT * from R");
      stmt.addBatch("CREATE VIEW r_server (sid, t, v, Ls, Lr) AS "
                      + "SELECT W.sid, W.t2, W.v2, CW.sid, PD.rid "
                      + "FROM W LEFT OUTER JOIN CW ON W.sid = CW.sid AND (W.t2 = CW.ts OR W.t2 = CW.te) "
                      + "  LEFT OUTER JOIN PD ON W.sid = PD.sid AND W.t2 = PD.t2");
      stmt.addBatch("CREATE VIEW f_distance_blocks (sid, val, dtype) AS "
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
                      + "GROUP BY d.sid, d.dtype");
      stmt.addBatch("CREATE VIEW f_status (t, sid, rid, val) AS "
                      + "SELECT a.t2, a.sid, a.rid, COUNT (b.rid) "
                      + "FROM CQ AS a INNER JOIN CQ AS b ON a.t2 >= b.t2 "
                      + "WHERE a.rid IS NOT NULL AND b.rid IS NOT NULL AND a.rid = b.rid "
                      + "GROUP BY a.t2, a.sid, a.rid");
      stmt.addBatch("CREATE VIEW assignments (t, sid, rid) AS "
                      + "SELECT t, sid, rid FROM f_status WHERE val = 2 ORDER BY t ASC");
      stmt.addBatch("CREATE VIEW assignments_r (t, rid) AS "
                      + "SELECT t, rid FROM assignments");
      stmt.addBatch("CREATE VIEW service_rate (val) AS "
                      + "SELECT CAST(CAST(A.NUM AS FLOAT) / CAST(A.DENOM AS FLOAT) * 10000 as INT)"
                      + "FROM ( "
                      + "SELECT (SELECT COUNT(*) FROM assignments_r) AS NUM, "
                      + "       (SELECT COUNT(*) FROM R) AS DENOM "
                      + "       FROM assignments_r FETCH FIRST ROW ONLY "
                      + ") A");
      stmt.addBatch("CREATE VIEW dist_base (val) AS "
                      + "SELECT SUM (ub) FROM UB");
      stmt.addBatch("CREATE VIEW dist_s_travel (sid, val) AS "
                      + "SELECT W.sid, SUM (COALESCE (dd, 0)) "
                      + "FROM W JOIN CW ON w.sid = cw.sid AND (t2 BETWEEN ts AND te) "
                      + "GROUP BY W.sid");
      stmt.addBatch("CREATE VIEW dist_s_cruising (sid, val) AS "
                      + "SELECT sid, val FROM f_distance_blocks WHERE dtype = true");
      stmt.addBatch("CREATE VIEW dist_s_service (sid, val) AS "
                      + "SELECT sid, val FROM f_distance_blocks WHERE dtype = false");
      stmt.addBatch("CREATE VIEW dist_s_base (val) AS "
                      + "SELECT SUM (sb) FROM S");
      stmt.addBatch("CREATE VIEW dist_r_base (val) AS "
                      + "SELECT SUM (rb) FROM R");
      stmt.addBatch("CREATE VIEW dist_r_unassigned (val) AS "
                      + "SELECT SUM (rb) FROM R LEFT JOIN assignments_r "
                      + "  ON R.rid = assignments_r.rid "
                      + "WHERE assignments_r.rid IS NULL");
      stmt.addBatch("CREATE VIEW dist_r_transit (rid, val) AS "
                      + "SELECT rid, SUM (COALESCE (dd, 0)) "
                      + "FROM CPD JOIN W ON CPD.sid = W.sid AND CPD.tp < W.t2 AND W.t2 <= CPD.td "
                      + "GROUP BY rid");
      stmt.addBatch("CREATE VIEW dist_r_detour (rid, val) AS "
                      + "SELECT rid, val-ub FROM UB JOIN dist_r_transit ON uid = rid");
      stmt.addBatch("CREATE VIEW dur_s_travel (sid, val) AS "
                      + "SELECT sid, te - ts FROM CW");
      stmt.addBatch("CREATE VIEW dur_r_pickup (rid, val) AS "
                      + "SELECT rid, tp - re FROM CPD");
      stmt.addBatch("CREATE VIEW dur_r_transit (rid, val) AS "
                      + "SELECT rid, td - tp FROM CPD");
      stmt.addBatch("CREATE VIEW dur_r_travel (rid, val) AS "
                      + "SELECT rid, td - re FROM CPD");
      stmt.addBatch("CREATE VIEW t_r_depart (rid, val) AS "
                      + "SELECT rid, tp FROM CPD");
      stmt.addBatch("CREATE VIEW t_s_depart (sid, val) AS "
                      + "SELECT sid, ts FROM CW");
      stmt.addBatch("CREATE VIEW t_r_arrive (rid, val) AS "
                      + "SELECT rid, td FROM CPD");
      stmt.addBatch("CREATE VIEW t_s_arrive (sid, val) AS "
                      + "SELECT sid, te FROM CW");
      stmt.addBatch("CREATE INDEX W_sid_t1 ON W (sid, t1)");
      stmt.addBatch("CREATE INDEX W_sid_t2 ON W (sid, t2)");
      stmt.addBatch("CREATE INDEX W_sid_v2 ON W (sid, v2)");
      stmt.addBatch("CREATE INDEX W_sid_t1_t2 ON W (sid, t1, t2)");
      stmt.executeBatch();
      conn.commit();
    } catch (SQLException e) {
      System.err.println("Storage.DBLoadDataModel(): "+"Encountered catastrophic exception.");
      if (e.getErrorCode() == 0) {
        System.err.println("(did you forget to call Storage.DBCreateNewInstance()?)");
      } else if (e.getErrorCode() == 20000) {
        System.err.println("(data model already exists from Storage.DBLoadBackup()?)");
      }
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
  public void DBLoadBackup(final String p) throws SQLException {
    this.CONNECTIONS_URL = "jdbc:derby:memory:jargo;createFrom="+p;
    try {
      this.setupDriver();
    } catch (ClassNotFoundException e) {
      System.out.println("Storage.DBLoadBackup(1): "+"Encountered catastrophic exception.");
      e.printStackTrace();
      System.exit(1);
    }
  }
  public void DBLoadRoadNetworkFromDB() throws SQLException {
    ConcurrentHashMap<Integer, int[]>    lu1 = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>> lu2 = new ConcurrentHashMap<>();
    try {
      final int[] output = this.DBQueryAllVertices();
      for (int i = 0; i < (output.length - 2); i += 3) {
        final int   v = output[(i + 0)];
        final int lng = output[(i + 1)];
        final int lat = output[(i + 2)];
        lu1.put(v, new int[] { lng, lat });
      }
    } catch (SQLException e) {
      throw e;
    }
    try {
      final int[] output = this.DBQueryAllEdges();
      for (int i = 0; i < (output.length - 3); i += 4) {
        final int v1 = output[(i + 0)];
        final int v2 = output[(i + 1)];
        final int dd = output[(i + 2)];
        final int nu = output[(i + 3)];
        if (!lu2.containsKey(v1)) {
          lu2.put(v1, new ConcurrentHashMap());
        }
        lu2.get(v1).put(v2, new int[] { dd, nu });
      }
    } catch (SQLException e) {
      throw e;
    }
    this.lu_vertices = lu1;
    this.lu_edges    = lu2;
  }
  public void DBLoadUsersFromDB() throws SQLException {
    ConcurrentHashMap<Integer, int[]> lu1 = new ConcurrentHashMap<>();
    Map<Integer, Boolean>             lu2 = new HashMap<>();
    try {
      final int[] output = this.DBQueryAllUsers();
      for (int i = 0; i < (output.length - 6); i += 7) {
        final int uid = output[(i + 0)];
        final int  uq = output[(i + 1)];
        final int  ue = output[(i + 2)];
        final int  ul = output[(i + 3)];
        final int  uo = output[(i + 4)];
        final int  ud = output[(i + 5)];
        final int  ub = output[(i + 6)];
        lu1.put(uid, new int[] { uid, uq, ue, ul, uo, ud, ub });
        if (uq > 0) {
          lu2.put(uid, (this.DBQueryRequestIsAssigned(uid).length > 0 ? true : false));
        }
      }
    } catch (SQLException e) {
      throw e;
    }
    this.lu_users   = lu1;
    this.lu_rstatus = lu2;
  }
  public void DBSaveBackup(final String p) throws SQLException {
    try (Connection conn = DriverManager.getConnection(CONNECTIONS_POOL_URL)) {
      CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE('"+p+"')");
      cs.execute();
    } catch (SQLException e) {
      throw e;
    }
  }
  public void _printSQLDriverStatistics() throws SQLException {
    PoolingDriver d = (PoolingDriver) DriverManager.getDriver(CONNECTIONS_DRIVER_URL);
    ObjectPool<? extends Connection> cp = d.getConnectionPool(CONNECTIONS_POOL_NAME);
    System.out.println("Connections: "+cp.getNumActive()+" active; "+cp.getNumIdle()+" idle");
  }
  public Connection _getConnection() throws SQLException {
    return DriverManager.getConnection(CONNECTIONS_POOL_URL);
  }
  private void setupDriver() throws SQLException, ClassNotFoundException {
    connection_factory = new DriverManagerConnectionFactory(CONNECTIONS_URL);
    poolableconnection_factory = new PoolableConnectionFactory(connection_factory, null);
    poolableconnection_factory.setPoolStatements(true);
    poolableconnection_factory.setDefaultAutoCommit(false);
    poolableconnection_factory.setMaxOpenPreparedStatements(STATEMENTS_MAX_COUNT);
    GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
    cfg.setMinIdle(100000);
    cfg.setMaxIdle(100000);
    cfg.setMaxTotal(100000);
    pool = new GenericObjectPool<>(poolableconnection_factory, cfg);
    poolableconnection_factory.setPool(pool);
    Class.forName("org.apache.commons.dbcp2.PoolingDriver");
    driver = (PoolingDriver) DriverManager.getDriver(CONNECTIONS_DRIVER_URL);
    driver.registerPool(CONNECTIONS_POOL_NAME, pool);
  }
  private void PSInit() {
    String INS = "INSERT INTO ";
    String UPD = "UPDATE ";
    String DEL = "DELETE FROM ";
    String SEL = "SELECT ";
    String q2  = "(?,?)";
    String q3  = "(?,?,?)";
    String q4  = "(?,?,?,?)";
    String q7  = "(?,?,?,?,?,?,?)";
    String q8  = "(?,?,?,?,?,?,?,?)";
    String q9  = "(?,?,?,?,?,?,?,?,?)";
    String q12 = "(?,?,?,?,?,?,?,?,?,?,?,?)";
    String q14 = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    this.lu_pstr.put("S0", INS+"V VALUES "+q3);
    this.lu_pstr.put("S1", INS+"E VALUES "+q4);
    this.lu_pstr.put("S2", INS+"UQ VALUES "+q2);
    this.lu_pstr.put("S3", INS+"UE VALUES "+q2);
    this.lu_pstr.put("S4", INS+"UL VALUES "+q2);
    this.lu_pstr.put("S5", INS+"UO VALUES "+q2);
    this.lu_pstr.put("S6", INS+"UD VALUES "+q2);
    this.lu_pstr.put("S7", INS+"UB VALUES "+q2);
    this.lu_pstr.put("S8", INS+"S VALUES "+q7);
    this.lu_pstr.put("S9", INS+"R VALUES "+q7);
    this.lu_pstr.put("S10", INS+"W VALUES "+q8);
    this.lu_pstr.put("S11", INS+"CW VALUES "+q9);
    this.lu_pstr.put("S12", INS+"PD VALUES "+q4);
    this.lu_pstr.put("S13", INS+"CPD VALUES "+q12);
    this.lu_pstr.put("S14", INS+"CQ VALUES "+q14);
    this.lu_pstr.put("S15", UPD+"E SET nu=? WHERE v1=? AND v2=?");
    this.lu_pstr.put("S131", UPD+"W SET nu=? WHERE v1=? AND v2=?");
    this.lu_pstr.put("S77", UPD+"CW SET te=?, ve=? WHERE sid=?");
    this.lu_pstr.put("S84", UPD+"PD SET t2=? WHERE v2=? AND rid=?");
    this.lu_pstr.put("S82", UPD+"CPD SET tp=? WHERE vp=? AND rid=?");
    this.lu_pstr.put("S83", UPD+"CPD SET td=? WHERE vd=? AND rid=?");
    this.lu_pstr.put("S76", DEL+"W WHERE sid=? AND t2>?");
    this.lu_pstr.put("S42", DEL+"PD WHERE rid=?");
    this.lu_pstr.put("S43", DEL+"CPD WHERE rid=?");
    this.lu_pstr.put("S80", DEL+"CQ WHERE sid=? AND t2>?");
    this.lu_pstr.put("S62", SEL+"COUNT (*) FROM V WHERE v<>0");
    this.lu_pstr.put("S64", SEL+"MIN (lng), MAX (lng), MIN (lat), MAX (lat) "
          + "FROM V WHERE v<>0");
    this.lu_pstr.put("S63", SEL+"COUNT (*) FROM E WHERE v1<>0 AND v2<>0");
    this.lu_pstr.put("S65", SEL+"MIN (dd), MAX (dd), SUM (dd) / COUNT (dd), "
          + "MIN (nu), MAX (nu), SUM (nu) / COUNT (nu) "
          + "FROM E WHERE v1<>0 AND v2<>0");
    this.lu_pstr.put("S46", SEL+"dd, nu FROM E WHERE v1=? AND v2=?");
    this.lu_pstr.put("S130", SEL+"lng, lat FROM V WHERE v=?");
    this.lu_pstr.put("S70", SEL+"sid, sq, se, sl, so, sd, sb FROM S WHERE sid=?");
    this.lu_pstr.put("S48", SEL+"sq, se FROM S WHERE sid=?");
    this.lu_pstr.put("S66", SEL+"COUNT (*) FROM S");
    this.lu_pstr.put("S75", SEL+"rid, rq, re, rl, ro, rd, rb FROM R WHERE rid=?");
    this.lu_pstr.put("S51", SEL+"rq, re, rl, ro, rd FROM R WHERE rid=?");
    this.lu_pstr.put("S67", SEL+"COUNT (*) FROM R");
    this.lu_pstr.put("S59", SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
          + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
          + "GROUP BY sid"
          + ") as b ON a.sid=b.sid AND ABS(a.t2-?)=b.tdiff AND a.t2<=?");
    this.lu_pstr.put("S128", SEL+"a.sid, a.t2, a.v2 FROM W AS a INNER JOIN ("
          + "SELECT sid FROM CW WHERE te>? OR (ve=0 AND sl>?)"
          + ") as b ON a.sid=b.sid INNER JOIN ("
          + "SELECT sid, MIN(ABS(t2-?)) as tdiff FROM W WHERE t2<=? AND v2<>0 "
          + "GROUP BY sid"
          + ") as c ON a.sid=c.sid AND ABS(a.t2-?)=c.tdiff AND a.t2<=?");
    this.lu_pstr.put("S60", SEL+"t, v FROM r_server WHERE sid=? ORDER BY t ASC");
    this.lu_pstr.put("S129", SEL+"t, v FROM r_server WHERE sid=? AND t>? ORDER BY t ASC");
    this.lu_pstr.put("S61", SEL+"t, v, Ls, Lr FROM r_server WHERE sid=?"
          + "AND (Ls IS NOT NULL OR Lr IS NOT NULL) ORDER BY t ASC");
    this.lu_pstr.put("S69", SEL+"t, v, Ls, Lr "
          + "FROM r_server LEFT JOIN CQ ON t=t2 and v=v2 and Lr=rid "
          + "WHERE r_server.sid=?"
          + "   AND (t>? OR v=0)"
          + "   AND (Ls IS NOT NULL OR Lr IS NOT NULL)"
          + "ORDER BY t ASC, o2 ASC");
    // A "timeout" of 30 seconds is hard-coded here
    this.lu_pstr.put("S68", SEL+"* FROM R WHERE re<=? AND ?<=re+30 AND rid NOT IN  "
          + "(SELECT rid FROM assignments_r)");
    this.lu_pstr.put("S85", SEL+"uq FROM UQ WHERE uid=?");
    this.lu_pstr.put("S86", SEL+"tp, td FROM CPD WHERE rid=?");
    this.lu_pstr.put("S73", SEL+"q2 FROM CQ WHERE sid=? AND t2<=? "
          + "ORDER BY t2 DESC, q2 DESC FETCH FIRST ROW ONLY");
    this.lu_pstr.put("S87", SEL+"t2, q2, o2 FROM CQ WHERE sid=? AND t2<=? "
          + "ORDER BY t2 DESC, o2 DESC FETCH FIRST ROW ONLY");
    this.lu_pstr.put("S100", SEL+"rid FROM assignments WHERE t>? AND sid=?");
    this.lu_pstr.put("S101", SEL+"rid FROM assignments WHERE t<=? AND sid=?");
    this.lu_pstr.put("S102", SEL+"* FROM service_rate");
    this.lu_pstr.put("S103", SEL+"* FROM dist_base");
    this.lu_pstr.put("S104", SEL+"val FROM dist_s_travel WHERE sid=?");
    this.lu_pstr.put("S105", SEL+"SUM (val) FROM dist_s_travel");
    this.lu_pstr.put("S106", SEL+"val FROM dist_s_cruising WHERE sid=?");
    this.lu_pstr.put("S107", SEL+"SUM (val) FROM dist_s_cruising");
    this.lu_pstr.put("S108", SEL+"val FROM dist_s_service WHERE sid=?");
    this.lu_pstr.put("S109", SEL+"SUM (val) FROM dist_s_service");
    this.lu_pstr.put("S110", SEL+"val FROM dist_s_base");
    this.lu_pstr.put("S111", SEL+"val FROM dist_r_base");
    this.lu_pstr.put("S112", SEL+"val FROM dist_r_detour WHERE rid=?");
    this.lu_pstr.put("S113", SEL+"SUM (val) FROM dist_r_detour");
    this.lu_pstr.put("S114", SEL+"val FROM dist_r_transit WHERE rid=?");
    this.lu_pstr.put("S115", SEL+"SUM (val) FROM dist_r_transit");
    this.lu_pstr.put("S116", SEL+"val FROM dur_s_travel WHERE sid=?");
    this.lu_pstr.put("S117", SEL+"SUM (val) FROM dur_s_travel");
    this.lu_pstr.put("S118", SEL+"val FROM dur_r_pickup WHERE rid=?");
    this.lu_pstr.put("S119", SEL+"SUM (val) FROM dur_r_pickup");
    this.lu_pstr.put("S120", SEL+"val FROM dur_r_transit WHERE rid=?");
    this.lu_pstr.put("S121", SEL+"SUM (val) FROM dur_r_transit");
    this.lu_pstr.put("S122", SEL+"val FROM dur_r_travel WHERE rid=?");
    this.lu_pstr.put("S123", SEL+"SUM (val) FROM dur_r_travel");
    this.lu_pstr.put("S124", SEL+"val FROM t_r_depart WHERE rid=?");
    this.lu_pstr.put("S125", SEL+"val FROM t_s_depart WHERE sid=?");
    this.lu_pstr.put("S126", SEL+"val FROM t_r_arrive WHERE rid=?");
    this.lu_pstr.put("S127", SEL+"val FROM t_s_arrive WHERE sid=?");
    this.lu_pstr.put("S133", SEL+"val FROM f_status WHERE rid=? AND t<=? "
        + "ORDER BY t DESC FETCH FIRST ROW ONLY");
    this.lu_pstr.put("S134", SEL+"sid, te FROM CW WHERE se<=? AND (?<te OR (ve=0 AND sl>?))");
    this.lu_pstr.put("S135", SEL+"t2, v2 FROM W WHERE sid=? AND t2=("
        + "SELECT t1 FROM W WHERE sid=? AND t1 <= ? AND ? < t2)");
    this.lu_pstr.put("S136", SEL+"* FROM V");
    this.lu_pstr.put("S137", SEL+"* FROM E");
    this.lu_pstr.put("S138", SEL+"val FROM dist_r_unassigned");
    this.lu_pstr.put("S139", UPD+"CPD SET te=? WHERE sid=?");
    this.lu_pstr.put("S140", UPD+"CQ SET tp=?, td=? WHERE rid=?");
    this.lu_pstr.put("S141", SEL+"* FROM r_user");
    this.lu_pstr.put("S142", SEL+"SUM (dd) FROM W WHERE sid=? AND t2>?");
    this.lu_pstr.put("S143", SEL+"* FROM R WHERE re<=? AND ?<=re+?");
    this.lu_pstr.put("S144", SEL+"t2, v2, rid FROM CQ WHERE sid=? AND t2>? ORDER BY o2 ASC");
    this.lu_pstr.put("S145", SEL+"te, ve FROM CW WHERE sid=?");
    this.lu_pstr.put("S147", SEL+"t2, v2 FROM W WHERE sid=? AND t2=("
        + "SELECT t1 FROM W WHERE sid=? AND v2=0)");
    this.lu_pstr.put("S148", SEL+"1 FROM assignments_r WHERE rid=?");
    this.lu_pstr.put("S149", SEL+"t2, v2 FROM W WHERE sid=? ORDER BY t2 ASC");
  }
  private PreparedStatement PS(final Connection conn, final String k) throws SQLException {
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
  private int[] DBFetch(
      final Connection conn, final String k, final int ncols, final Integer... values)
  throws SQLException {
    int[] output = new int[] { };
    try {
      PreparedStatement p = PS(conn, k);
      for (int i = 0; i < values.length; i++) {
        if (values[i] == null) {
          p.setNull((i + 1), java.sql.Types.INTEGER);
        } else {
          p.setInt ((i + 1), values[i]);
        }
      }
      ResultSet res = p.executeQuery();  // <-- if stuck here, investigate locks
      if (res.last() == true) {
        output = new int[(ncols*res.getRow())];
        res.first();
        do {
          for (int j = 1; j <= ncols; j++) {
            output[((res.getRow() - 1)*ncols + (j - 1))] = res.getInt(j);
          }
        } while (res.next());
      }
      res.close();
      p.close();
    } catch (SQLException e) {
      throw e;
    }
    return output;
  }
  private void PSAdd(PreparedStatement p, final Integer... values) throws SQLException {
    p.clearParameters();
    for (int i = 0; i < values.length; i++) {
      if (values[i] == null) {
        p.setNull((i + 1), java.sql.Types.INTEGER);
      } else {
        p.setInt ((i + 1), values[i]);
      }
    }
    try {
      p.addBatch();
    } catch (SQLException e) {
      throw e;
    }
  }
  private void PSSubmit(PreparedStatement... statements) throws SQLException {
    try {
      for (PreparedStatement p : statements) {
        p.executeBatch();
        p.close();
      }
    } catch (SQLException e) {
      throw e;
    }
  }
}
