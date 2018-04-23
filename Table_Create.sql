CREATE TABLE Flight (
	Flight_Number CHAR(8),
	Departure_City CHAR(32),
	Destination_City CHAR(32),
	Departure_Time DATE,
	Arrival_Time DATE,
	Fare REAL,
	Seats_Limit INTEGER,
	PRIMARY KEY (Flight_Number)
);

CREATE TABLE Customer (
	ID CHAR(16),
	Name CHAR(32),
	Nationality CHAR(32),
	Passport_Number CHAR(16),
	PRIMARY KEY (ID),
	UNIQUE (Passport_Number)
);

CREATE TABLE Connection (
	First_Flight CHAR(8) NOT NULL,
	Second_Flight CHAR(8),
	Third_Flight CHAR(8),
	ID INTEGER,
	FOREIGN KEY (First_Flight) REFERENCES Flight(Flight_Number),
	FOREIGN KEY (Second_Flight) REFERENCES Flight(Flight_Number),
	FOREIGN KEY (Third_Flight) REFERENCES Flight(Flight_Number),
	PRIMARY KEY (ID)
);

CREATE TABLE Booking (
	Connection_ID INTEGER,
	Customer_ID CHAR(16),
	Fare REAL,
	ID CHAR(16),
	FOREIGN KEY (Connection_ID) REFERENCES Connection(ID),
	FOREIGN KEY (Customer_ID) REFERENCES Customer(ID),
	PRIMARY KEY (ID)
);
