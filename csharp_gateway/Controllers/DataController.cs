using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using System.Text.Json;
using System.Threading.Tasks;
using CSharpGateway.Services; // ✅ Required for IKafkaProducerService

namespace CSharpGateway.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DataController : ControllerBase
    {
        private readonly ILogger<DataController> _logger;
        private readonly IKafkaProducerService _kafkaProducer;

        // ✅ Inject logger and Kafka producer
        public DataController(ILogger<DataController> logger, IKafkaProducerService kafkaProducer)
        {
            _logger = logger;
            _kafkaProducer = kafkaProducer;
        }

        [HttpPost]
        public async Task<IActionResult> Post([FromBody] object payload)
        {
            _logger.LogInformation("Received data: {Payload}", payload.ToString());

            // ✅ Replace Kafka producer config with service call
            var json = JsonSerializer.Serialize(payload);
            await _kafkaProducer.ProduceAsync("input-data", json);

            _logger.LogInformation("Sent to Kafka topic: input-data");

            return Ok(new { status = "sent-to-kafka", payload });
        }
    }
}
