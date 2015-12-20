package com.venki;

import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.json.*;
 
@SuppressWarnings("serial")
public class JqueryDatatablePluginDemo extends HttpServlet {
 
 private String GLOBAL_SEARCH_TERM;
 private String COLUMN_NAME;
 private String DIRECTION;
 private int INITIAL;
 private int RECORD_SIZE;
 private String ID_SEARCH_TERM,NAME_SEARCH_TERM,PLACE_SEARCH_TERM,CITY_SEARCH_TERM,
 STATE_SEARCH_TERM,PHONE_SEARCH_TERM;
 
 public void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
 
  String[] columnNames = { "id", "message", "status", "senderId", "messageId","mobileNumber" };
 
  JSONObject jsonResult = new JSONObject();
  int listDisplayAmount = 10;
  int start = 0;
  int column = 0;
  String dir = "asc";
  String pageNo = request.getParameter("iDisplayStart");
  String pageSize = request.getParameter("iDisplayLength");
  String colIndex = request.getParameter("iSortCol_0");
  String sortDirection = request.getParameter("sSortDir_0");
   
  if (pageNo != null) {
   start = Integer.parseInt(pageNo);
   if (start < 0) {
    start = 0;
   }
  }
  if (pageSize != null) {
   listDisplayAmount = Integer.parseInt(pageSize);
   if (listDisplayAmount < 10 || listDisplayAmount > 50) {
    listDisplayAmount = 10;
   }
  }
  if (colIndex != null) {
   column = Integer.parseInt(colIndex);
   if (column < 0 || column > 5)
    column = 0;
  }
  if (sortDirection != null) {
   if (!sortDirection.equals("asc"))
    dir = "desc";
  }
 
  String colName = columnNames[column];
  int totalRecords= -1;
  try {
   totalRecords = getTotalRecordCount();
  } catch (SQLException e1) {
   e1.printStackTrace();
  }
 
  RECORD_SIZE = listDisplayAmount;
  GLOBAL_SEARCH_TERM = request.getParameter("sSearch");
  ID_SEARCH_TERM=request.getParameter("sSearch_0");
  NAME_SEARCH_TERM=request.getParameter("sSearch_1");
  PLACE_SEARCH_TERM=request.getParameter("sSearch_2");
  CITY_SEARCH_TERM=request.getParameter("sSearch_3");
  STATE_SEARCH_TERM=request.getParameter("sSearch_4");
  PHONE_SEARCH_TERM=request.getParameter("sSearch_5");
  COLUMN_NAME = colName;
  DIRECTION = dir;
  INITIAL = start;
 
  try {
   jsonResult = getPersonDetails(totalRecords, request);
  } catch (ClassNotFoundException e) {
   e.printStackTrace();
  } catch (SQLException e) {
   e.printStackTrace();
  }
 
  response.setContentType("application/json");
  response.setHeader("Cache-Control", "no-store");
  PrintWriter out = response.getWriter();
  out.print(jsonResult);
 
 }
 
 public JSONObject getPersonDetails(int totalRecords, HttpServletRequest request)
   throws SQLException, ClassNotFoundException {
 
  int totalAfterSearch = totalRecords;
  JSONObject result = new JSONObject();
  JSONArray array = new JSONArray();
  String searchSQL = "";
 
  try {
   Class.forName("com.mysql.jdbc.Driver");
  } catch (ClassNotFoundException e) {
   e.printStackTrace();
  }
  String dbConnectionURL = "jdbc:mysql://localhost:3306/comm?user=root&password=root";
  Connection con = DriverManager.getConnection(dbConnectionURL);
  String sql = "SELECT * FROM comm.sms_data " + "WHERE ";
 
  String globeSearch = "id like '%" + GLOBAL_SEARCH_TERM + "%'"
    + "or message like '%" + GLOBAL_SEARCH_TERM + "%'"
    + "or messageId like '%" + GLOBAL_SEARCH_TERM + "%'"
    + "or senderId like '%" + GLOBAL_SEARCH_TERM + "%'"
    + "or status like  '%" + GLOBAL_SEARCH_TERM + "%'"
    + "or mobileNumber like '%" + GLOBAL_SEARCH_TERM + "%'";
   
  String idSearch="id like " + ID_SEARCH_TERM + "";
  String nameSearch="message like '%" + NAME_SEARCH_TERM + "%'";
  String placeSearch=" messageId like '%" + PLACE_SEARCH_TERM + "%'";
  String citySearch=" senderId like '%" + CITY_SEARCH_TERM + "%'";
  String stateSearch=" status like '%" + STATE_SEARCH_TERM + "%'"; 
  String phoneSearch=" mobileNumber like '%" + PHONE_SEARCH_TERM + "%'";
        System.out.println(phoneSearch);
  if (GLOBAL_SEARCH_TERM != "") {
   searchSQL = globeSearch;
  }
  else if(ID_SEARCH_TERM !="")
  {
   searchSQL=idSearch;
  }
  else if(NAME_SEARCH_TERM !="")
  {
   searchSQL=nameSearch;
  }
  else if(PLACE_SEARCH_TERM!="")
  {
   searchSQL=placeSearch;
  }
  else if(CITY_SEARCH_TERM!="")
  {
   searchSQL=citySearch;
  }
  else if(STATE_SEARCH_TERM!="")
  {
   searchSQL=stateSearch;
  }
  else if(PHONE_SEARCH_TERM!=null)
  {
   searchSQL=phoneSearch;
    
  }
      
  sql += searchSQL;
  sql += " order by " + COLUMN_NAME + " " + DIRECTION;
  sql += " limit " + INITIAL + ", " + RECORD_SIZE;
         
        //for searching
  PreparedStatement stmt = con.prepareStatement(sql);
  ResultSet rs = stmt.executeQuery();
 
  while (rs.next()) {
   JSONArray ja = new JSONArray();
   ja.put(rs.getString("id"));
   ja.put(rs.getString("message"));
   ja.put(rs.getString("status"));
   ja.put(rs.getString("senderId"));
   ja.put(rs.getString("messageId"));
   ja.put(rs.getString("mobileNumber"));
   array.put(ja); 
  }
  System.out.println(array);
  stmt.close();
  rs.close();
 
  String query = "SELECT " + "COUNT(*) as count " + "FROM " + "comm.sms_data " + "WHERE ";
 
  //for pagination
  if (GLOBAL_SEARCH_TERM != ""||ID_SEARCH_TERM != "" || NAME_SEARCH_TERM != ""
||PLACE_SEARCH_TERM != ""||CITY_SEARCH_TERM != ""|| STATE_SEARCH_TERM != ""
|| PHONE_SEARCH_TERM != "" ) {
   query += searchSQL;
 
    
   PreparedStatement st = con.prepareStatement(query);
   ResultSet results = st.executeQuery();
 
   if (results.next()) {
    totalAfterSearch = results.getInt("count");
   }
   st.close();
   results.close();
   con.close();
  }
  try {
   result.put("iTotalRecords", totalRecords);
   result.put("iTotalDisplayRecords", totalAfterSearch);
   result.put("aaData", array);
  } catch (Exception e) {
 
  }
 
  return result;
 }
 
 public int getTotalRecordCount() throws SQLException {
 
  int totalRecords = -1;
  String sql = "SELECT COUNT(*) as count FROM comm.sms_data";
        String dbConnectionURL = "jdbc:mysql://localhost:3306/comm?user=root&password=root";
  try {
   Class.forName("com.mysql.jdbc.Driver");
  } catch (ClassNotFoundException e) {
   e.printStackTrace();
  }
 
  Connection con = DriverManager.getConnection(dbConnectionURL);
 
  PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
 
  if (resultSet.next()) {
   totalRecords = resultSet.getInt("count");
  }
  resultSet.close();
  statement.close();
  con.close();
 
  return totalRecords;
 }
 
}
