class PagesController < ApplicationController

  skip_before_filter :require_login, only: [:index, :about]


  def index
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
end
