class PagesController < ApplicationController

  skip_before_filter :require_login, only: [:index, :about, :note, :notejs]
  before_action :set_note, only: [:note, :notejs]


  def index
    @latest_public_notes = Note.latest_public_notes
    render layout: "homepage"
  end

  def about 

  end

  def notebook 
    render layout: "notebook"
  end

  def notebookjs
    render layout: "notebookjs"
  end

  def note
    render layout: "note"   
  end

  def notejs
    render layout: "notejs"   
  end

  private
  def set_note
    @note = nil
    if not params[:id].nil?
      n =  Note.find(params[:id])
      @note = n if n.public 
      if @note.nil? and logged_in?
        @note = n if (n.user_id == @current_user.id)
      end
    end
  end
end
