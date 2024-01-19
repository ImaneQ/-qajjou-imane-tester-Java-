package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
	private static final Logger logger = LogManager.getLogger("ParkingServiceTest");

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	Ticket ticket;
	String vehicleRegNumber;
	int id;
	int parkingNumber;

	@BeforeEach
	private void setUpPerTest() {
		logger.debug("Je rentre dans la méthode setUpPerTest()");
		try {

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	// test de l’appel de la méthode ProcessIncomingVehicle() où tout se déroule
	// comme attendu.
	@Test
	public void testProcessIncomingVehicle() {
//		GIVEN

		vehicleRegNumber = "ABCDEF";
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket = new Ticket(id, parkingSpot, vehicleRegNumber, 0,
				new Date(System.currentTimeMillis() - (60 * 60 * 1000)), null);
		logger.debug(this.ticket);
		ticketDAO.saveTicket(ticket);
		try {

//		WHEN

			when(inputReaderUtil.readSelection()).thenReturn(1);
			parkingService.processIncomingVehicle();

//		THEN

			verify(ticketDAO).saveTicket(ticket);
			verify(inputReaderUtil).readSelection();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// exécution du test dans le cas ou la méthode updateTicket() de ticketDAO
	// renvoie false lors de l'appel de processExitingVehicle
	@Test
	public void processExitingVehicleTestUnableUpdate() {
//		GIVEN
//		vehicleRegNumber = "ABCDEF";
//		logger.debug(ticket);
//		ticket = ticketDAO.getTicket("ABCDEF");
		
//		logger.debug(ticket);
		ticketDAO.updateTicket(ticket);
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		logger.debug(ticket);
		parkingSpot.setAvailable(true);

//		WHEN

		when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
		when(ticketDAO.updateTicket(ticket)).thenReturn(false);

		parkingService.processExitingVehicle();

//		THEN

		verify(ticketDAO).getTicket(vehicleRegNumber);
		verify(ticketDAO).updateTicket(ticket);
		assertFalse(ticketDAO.updateTicket(ticket));

	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat
	// l’obtention d’un spot dont l’ID est 1 et qui est disponible.
	@Test
	public void testGetNextParkingNumberIfAvailable() {

		logger.debug("Je rentre dans la méthode testGetNextParkingNumberIfAvailable()");

//		GIVEN
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

//		WHEN

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);

		parkingService.getNextParkingNumberIfAvailable();

//		THEN

		verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
		assertEquals(parkingSpot.getId(), 1);
		assertEquals(parkingSpotDAO.updateParking(parkingSpot), true);
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable()
	// avec pour résultat aucun spot disponible (la méthode renvoie null).
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

//		WHEN

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

//		THEN
		assertNull(parkingService.getNextParkingNumberIfAvailable());
		verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
	}

	/*
	 * test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	 * résultat aucun spot (la méthode renvoie null) car l’argument saisi par
	 * l’utilisateur concernant le type de véhicule est erroné (par exemple,
	 * l’utilisateur a saisi 3 .
	 */
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

//		GIVEN
		ParkingSpot parkingSpot = null;
		parkingSpotDAO.updateParking(parkingSpot);

//		WHEN

		when(inputReaderUtil.readSelection()).thenReturn(3);

		parkingService.getNextParkingNumberIfAvailable();

//		THEN

		verify(inputReaderUtil).readSelection();
		verify(parkingSpotDAO).updateParking(parkingSpot);
		assertNull(parkingSpot);
	}

	/*
	 * Complétez le test existant : processExitingVehicleTest ○ Ce test doit
	 * également mocker l’appel à la méthode getNbTicket() implémentée lors de
	 * l’étape précédente.
	 */
	@Test
	@DisplayName("processExitingVehicleCheckThatUpdateParkingMethodCalledTest()")
	public void processExitingVehicleCheckThatUpdateParkingMethodCalledTest() {

		logger.debug("Je rentre dans la méthode processExitingVehicleTest()");
		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		logger.debug(ticket);

		try {

//		GIVEN


			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			Date outTime = new Date();
			ticket.setOutTime(outTime);
			ParkingSpot parkingSpot = ticket.getParkingSpot();
			ticket.setParkingSpot(parkingSpot);

//			WHEN
//			when(inputReaderUtil.readSelection()).thenReturn("ABCDEF");

			when(parkingSpotDAO.updateParking(ticket.getParkingSpot())).thenReturn(true);
			when(ticketDAO.getNbTicket(anyString())).thenReturn(4);

			parkingService.processExitingVehicle();

//			THEN

			verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
			assertTrue(parkingSpotDAO.updateParking(ticket.getParkingSpot()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
