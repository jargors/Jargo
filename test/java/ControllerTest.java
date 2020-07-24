package com.github.jargors.test;

import com.github.jargors.sim.Controller;
import com.github.jargors.sim.Storage;

class ControllerTest {
  public static void main(String[] args) {
    loadRoadNetworkFromFile();
    loadProblem();
  }

  static void loadRoadNetworkFromFile() {
    try {
      Controller ctrl = new Controller();
      ctrl.instanceNew();
      ctrl.instanceInitialize();
      ctrl.loadRoadNetworkFromFile("resource/cd0.rnet");
      Storage strg = ctrl.getRefStorage();
      final int nedges = strg.DBQueryEdgesCount()[0];
      final int nnodes = strg.DBQueryVerticesCount()[0];
      System.out.printf("loadRoadNetworkFromFile 1... %s\n", nedges == 104 ? "pass" : "fail (" + nedges  + ")");
      System.out.printf("loadRoadNetworkFromFile 2... %s\n", nnodes ==  46 ? "pass" : "fail (" + nnodes  + ")");
      ctrl.instanceClose();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  static void loadProblem() {
    try {
      Controller ctrl = new Controller();
      ctrl.instanceNew();
      ctrl.instanceInitialize();
      ctrl.loadRoadNetworkFromFile("resource/cd0.rnet");
      ctrl.gtreeLoad("resource/cd00DN.gtree");
      ctrl.loadProblem("resource/cd0-1-0002.instance");
      Storage strg = ctrl.getRefStorage();
      final int nreqs = strg.DBQueryRequestsCount()[0];
      final int nserv = strg.DBQueryServersCount()[0];
      System.out.printf("loadProblem 1... %s\n", nreqs == 4 ? "pass" : "fail (" + nreqs  + ")");
      System.out.printf("loadProblem 2... %s\n", nserv == 2 ? "pass" : "fail (" + nserv  + ")");
      ctrl.instanceClose();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
