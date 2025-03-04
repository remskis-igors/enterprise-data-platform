library(shiny)

ui <- fluidPage(
  titlePanel("R Shiny Dashboard"),
  sidebarLayout(
    sidebarPanel(
      helpText("Analytics dashboard")
    ),
    mainPanel(
      plotOutput("distPlot")
    )
  )
)

server <- function(input, output) {
  output$distPlot <- renderPlot({
    hist(rnorm(1000))
  })
}

shinyApp(ui = ui, server = server)
