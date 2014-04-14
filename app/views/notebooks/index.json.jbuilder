#json.array!(@notebooks) do |notebook|
#  json.extract! notebook, :id, :name, :user_id
#  json.url notebook_url(notebook, format: :json)
#jend
json.array! @notebooks, :id, :name
