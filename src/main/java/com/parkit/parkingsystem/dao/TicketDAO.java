package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

//
public class TicketDAO {
	private static final Logger logger = LogManager.getLogger("TicketDAO");
	public DataBaseConfig dataBaseConfig = new DataBaseConfig();

	public boolean saveTicket(Ticket ticket) {
		logger.info("création d'un nouveau ticket dans saveTicket()");
		Connection con = null;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
		
			ps.setInt(1, ticket.getParkingSpot().getId());
			ps.setString(2, ticket.getVehicleRegNumber());
			ps.setDouble(3, ticket.getPrice());
			ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
			ps.setTimestamp(5, (ticket.getOutTime() == null) ? null :
				(new Timestamp(ticket.getOutTime().getTime())));
			logger.debug("test logger geTimeOut dans saveTicket");
			logger.debug(ticket.getOutTime());
			return ps.execute();
		} catch (Exception ex) {
			logger.error("Error fetching next available slot", ex);
		    ex.printStackTrace();
		} finally {
			dataBaseConfig.closeConnection(con);
			return false;
		}

	}

	public Ticket getTicket(String vehicleRegNumber) {
		logger.info("getTicket() récupération du ticket généré");
		Connection con = null;
		Ticket ticket = null;

		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
			ps.setString(1, vehicleRegNumber);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), 
						ParkingType.valueOf(rs.getString(6)), false);
				
				ticket = new Ticket(rs.getInt(2), parkingSpot, vehicleRegNumber, 
						rs.getDouble(3), rs.getTimestamp(4), rs.getTimestamp(5));
		
				logger.debug("test logger geTimeOut dans getTicket");
				logger.debug(ticket.getOutTime());
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
		} catch (Exception ex) {
			logger.error("Error fetching next available slot", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
			return ticket;
		}
	}
	public Ticket getLastTicket(String vehicleRegNumber) {
		logger.info("getTicket() récupération du ticket généré");
		Connection con = null;
		Ticket ticket = null;

		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.GET_LAST_TICKET);
			ps.setString(1, vehicleRegNumber);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
				ticket = new Ticket(rs.getInt(2), parkingSpot, vehicleRegNumber, rs.getDouble(3), rs.getTimestamp(4), rs.getTimestamp(5));
		
				logger.debug("test logger geTimeOut dans getTicket");
				logger.debug(ticket.getOutTime());
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
		} catch (Exception ex) {
			logger.error("Error fetching next available slot", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
			return ticket;
		}
	}

	public boolean updateTicket(Ticket ticket) throws ClassNotFoundException {
		logger.debug("updateTicket() modification du ticket généré pour insérer heure de sortie & prix");

		Connection con = null;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);

			ps.setDouble(1, ticket.getPrice());
		    logger.debug("Price set successfully");

		    ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
		    logger.debug("OutTime set successfully");

		    ps.setInt(3, ticket.getId());
		    logger.debug("ID set successfully");

		    ps.execute();
		    logger.debug("Update executed successfully");

			return true;
		} catch (SQLException | NullPointerException ex) {
		    logger.error("Error saving ticket info", ex);
		}finally {
			dataBaseConfig.closeConnection(con);
		}
		return false;
	}

	public int getNbTicket(String vehicleRegNb) {
		logger.debug("Je rentre dans la méthode getNbTicket");
		Connection con = null;
		int records = 0;

		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.GET_COUNT_FOR_VEHICLE);
			ps.setString(1, vehicleRegNb);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				records = rs.getInt("COUNT");
				logger.debug("Nombre de tickets pour le véhicule " + vehicleRegNb + " : " + records);
			}

			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);

		} catch (Exception ex) {
			logger.error("Error fetching next available slot", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
			return records;
		}
	}
}
