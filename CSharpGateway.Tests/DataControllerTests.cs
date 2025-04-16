using Xunit;
using Moq;
using Microsoft.Extensions.Logging;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using CSharpGateway.Controllers;
using CSharpGateway.Services;


namespace CSharpGateway.Tests
{
    public class DataControllerTests
    {
        [Fact]
        public async Task Post_ValidPayload_CallsKafkaProducerAndReturnsOk()
        {
            // Arrange
            var mockLogger = new Mock<ILogger<DataController>>();
            var mockProducer = new Mock<IKafkaProducerService>();
            var controller = new DataController(mockLogger.Object, mockProducer.Object);
            var testPayload = new { key = "value" };

            // Act
            var result = await controller.Post(testPayload);

            // Assert
            var okResult = Assert.IsType<OkObjectResult>(result);
            mockProducer.Verify(p => p.ProduceAsync("input-data", It.IsAny<string>()), Times.Once);
        }
    }
}
