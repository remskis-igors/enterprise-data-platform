FROM rocker/shiny:latest
WORKDIR /srv/shiny-server
COPY app.R .
EXPOSE 3838
CMD ["R", "-e", "shiny::runApp('/srv/shiny-server', host='0.0.0.0', port=3838)"]
