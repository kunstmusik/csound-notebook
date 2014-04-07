class CreateNotes < ActiveRecord::Migration
  def change
    create_table :notes do |t|
      t.string :title
      t.text :orc
      t.text :sco
      t.references :notebook, index: true
      t.boolean :public

      t.timestamps
    end
  end
end
