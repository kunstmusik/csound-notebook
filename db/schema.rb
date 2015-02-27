# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20150227025208) do

  create_table "notebooks", force: :cascade do |t|
    t.string   "name",       limit: 255
    t.integer  "user_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "notebooks", ["user_id"], name: "index_notebooks_on_user_id"

  create_table "notes", force: :cascade do |t|
    t.string   "title",       limit: 255
    t.text     "orc"
    t.text     "sco"
    t.integer  "notebook_id"
    t.boolean  "public"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "user_id"
    t.boolean  "livesco",                 default: false
  end

  add_index "notes", ["notebook_id"], name: "index_notes_on_notebook_id"
  add_index "notes", ["user_id"], name: "index_notes_on_user_id"

  create_table "users", force: :cascade do |t|
    t.string   "email",                           limit: 255, null: false
    t.string   "crypted_password",                limit: 255, null: false
    t.string   "salt",                            limit: 255, null: false
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "remember_me_token",               limit: 255
    t.datetime "remember_me_token_expires_at"
    t.string   "activation_state",                limit: 255
    t.string   "activation_token",                limit: 255
    t.datetime "activation_token_expires_at"
    t.string   "username",                        limit: 255
    t.string   "reset_password_token",            limit: 255
    t.datetime "reset_password_token_expires_at"
    t.datetime "reset_password_email_sent_at"
  end

  add_index "users", ["activation_token"], name: "index_users_on_activation_token"
  add_index "users", ["email"], name: "index_users_on_email", unique: true
  add_index "users", ["remember_me_token"], name: "index_users_on_remember_me_token"
  add_index "users", ["reset_password_token"], name: "index_users_on_reset_password_token"

end
