class PagesController < ApplicationController

  skip_before_filter :require_login, only: [:index, :about]


  def index

  end

  def about 

  end

end
