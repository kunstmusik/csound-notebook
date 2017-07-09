-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(username, email, pass)
VALUES (:username, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET username = :username, email = :email,
    pass = :pass
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE email = :email 

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id


-- :name get-note :? :1
-- :doc retrieve a note give the noteId
SELECT * from notes
WHERE note_id = :note-id

-- :name get-note-for-user :? :1
-- :doc retrieve a note give the username and noteId
SELECT * from notes
WHERE note_id = :note-id
AND user_id = (select id from users where username = :username) 

-- :name create-note! :! :n
-- :create a new note
INSERT INTO notes
(orc, sco, note_id, is_live, is_public, user_id)
VALUES
(:orc, :sco, :note-id, :is-live, :is-public, :user-id)
