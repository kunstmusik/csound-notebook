class AddLiveScoToNotes < ActiveRecord::Migration
  def change
    add_column :notes, :livesco, :boolean, :default => false
  end
end
