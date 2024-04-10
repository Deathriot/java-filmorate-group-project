# java-filmorate

Template repository for Filmorate project.
![ER-Diagram](https://github.com/Deathriot/java-filmorate-group-project/assets/127441142/5a790123-379f-43b7-a88f-41fda5502335)


# СПИСОК ТАБЛИЦ:

FILMS - список фильмов
GENRE - список жанров фильмов
FILM_GENRE - таблица для связи фильмов с жанрами (у одного фильма может быть несколько жанров)
RATING - список возрастных ограничений (рейтинг MPA)
USERS - список пользователей
USER_FILM - список пользователей, поставивших лайк фильму
FRIENDS - список друзей пользователя
DIRECTOR - список режиссеров
FILM_DIRECTOR - таблица для связи фильма с режиссерами
REVIEWS - список отзывов (хранит в себе id пользователя, который оставил отзыв, и id фильма, к которому отзыв оставили
USER_REVIEW_RATE - таблица для связи оценки (лайк/дизлайк) отзывов от пользовтелей
FEEDS - лента событий (постановка\удаления оценки, отзывов, добавление и удаления друзей) каждого пользователя


Примеры запросов:

1. Получить 10 самых популярных фильмов:

```
SELECT F.*, R.*, COUNT(UF.FILM_ID) AS LIKES
FROM FILMS AS F
LEFT JOIN RATING AS R ON R.RATING_ID = F.RATING
LEFT JOIN USER_FILM AS UF ON F.FILM_ID = UF.FILM_ID
GROUP BY F.FILM_ID
HAVING LIKES > 0
ORDER BY LIKES DESC
LIMIT 10;

```

2. Список друзей пользователя

```
SELECT * FROM USERS 
WHERE USER_ID IN (SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?);

```

3. Список общих друзей

```
SELECT U.*
FROM USERS AS U
JOIN FRIENDS F1 ON U.USER_ID = F1.FRIEND_ID AND F1.USER_ID=?
JOIN FRIENDS F2 ON U.USER_ID = F2.FRIEND_ID AND F2.USER_ID=?

```

4. Список всех отзывов о фильме
   
 ```
SELECT *
FROM REVIEWS
WHERE FILM_ID=?

```
5. Список всех режиссеров

```
SELECT *
FROM DIRECTOR

```

6. Получить ленту событий пользователя

```
SELECT *
FROM FEEDS
WHERE USER_ID=?

```
