class Note < ActiveRecord::Base
  belongs_to :notebook
  belongs_to :user

  def self.latest_public_notes
    Note.where(:public => true).order(:updated_at => :desc).limit(10)
  end
end
