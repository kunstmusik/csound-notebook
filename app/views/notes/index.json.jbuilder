json.array!(@notes) do |note|
  json.extract! note, :id, :title, :orc, :sco, :notebook_id, :public
  json.url note_url(note, format: :json)
end
