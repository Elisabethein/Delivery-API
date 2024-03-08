CREATE TABLE Weather (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         station VARCHAR(255),
                         WMO VARCHAR(255),
                         air DOUBLE,
                         wind DOUBLE,
                         weather VARCHAR(255),
                         observation TIMESTAMP
);

CREATE TABLE RBF (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        city VARCHAR(255),
                        vehicle VARCHAR(255),
                        fee DOUBLE
);

CREATE TABLE ATEF (
                        id      INT PRIMARY KEY AUTO_INCREMENT,
                        borders    VARCHAR(255),
                        fee     DOUBLE
);

CREATE TABLE WSEF (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    borders    VARCHAR(255),
    fee     DOUBLE
);

CREATE TABLE WPEF (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    containing    VARCHAR(255),
    fee     DOUBLE
);