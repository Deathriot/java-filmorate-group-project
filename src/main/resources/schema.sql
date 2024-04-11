CREATE TABLE IF NOT EXISTS RATING
(
    RATING_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    RATING_NAME VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS GENRE
(
    GENRE_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    GENRE_NAME    VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS DIRECTOR
(
    DIRECTOR_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME        VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    TITLE          VARCHAR(100) NOT NULL,
    DESCRIPTION   VARCHAR(200) NOT NULL,
    RELEASE_DATE  DATE    NOT NULL,
    DURATION      INTEGER      NOT NULL,
    RATING INTEGER REFERENCES RATING (RATING_ID)
);

CREATE TABLE IF NOT EXISTS USERS
(
    USER_ID  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    VARCHAR(255) CHECK (EMAIL <> '') NOT NULL UNIQUE,
    LOGIN    VARCHAR(50) CHECK (LOGIN <> '') NOT NULL UNIQUE,
    NAME     VARCHAR(50),
    BIRTHDAY DATE        NOT NULL
);

CREATE TABLE IF NOT EXISTS USER_FILM
(
    USER_ID INTEGER NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    FILM_ID INTEGER NOT NULL REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    CONSTRAINT USER_FILM_PK PRIMARY KEY (USER_ID, FILM_ID)
);

CREATE TABLE IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER NOT NULL REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    GENRE_ID INTEGER NOT NULL REFERENCES GENRE (GENRE_ID) ON DELETE CASCADE,
    CONSTRAINT FILM_GENRE_PK PRIMARY KEY (FILM_ID, GENRE_ID)
);

CREATE TABLE IF NOT EXISTS FILM_DIRECTOR
(
    FILM_ID     INTEGER NOT NULL REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    DIRECTOR_ID INTEGER NOT NULL REFERENCES DIRECTOR (DIRECTOR_ID) ON DELETE CASCADE,
    CONSTRAINT FILM_DIRECTOR_PK PRIMARY KEY (FILM_ID, DIRECTOR_ID)
);

CREATE TABLE IF NOT EXISTS FRIENDS
(
    USER_ID         INTEGER NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    FRIEND_ID       INTEGER NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    CONSTRAINT FRIENDS_PK PRIMARY KEY (USER_ID, FRIEND_ID)
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    REVIEW_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    CONTENT     VARCHAR(200) NOT NULL,
    IS_POSITIVE BIT NOT NULL,
    USER_ID     INTEGER NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    FILM_ID     INTEGER NOT NULL REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    USEFUL      INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS USER_REVIEW_RATE
(
    USER_ID     INTEGER NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    REVIEW_ID   INTEGER NOT NULL REFERENCES REVIEWS (REVIEW_ID) ON DELETE CASCADE,
    IS_POSITIVE BIT NOT NULL,
    CONSTRAINT USER_REVIEW_RATE_PK PRIMARY KEY (USER_ID, REVIEW_ID)
);

CREATE TABLE IF NOT EXISTS FEEDS(
    EVENT_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EVENT_TYPE VARCHAR(10) NOT NULL,
    OPERATION VARCHAR(10) NOT NULL,
    USER_ID INTEGER REFERENCES USERS (USER_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    ENTITY_ID INTEGER,
    EVENT_TIMESTAMP TIMESTAMP NOT NULL
);