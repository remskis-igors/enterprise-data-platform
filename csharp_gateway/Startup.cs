using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using CSharpGateway.Services;
namespace CSharpGateway
{
    public class Startup
    {
        // Registers services for dependency injection
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddControllers();

            // Register Kafka producer service so it can be injected into controllers
            services.AddSingleton<IKafkaProducerService, KafkaProducerService>();
        }

        // Configures the HTTP request pipeline
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage(); // Developer-friendly error page
            }

            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                // Enables route mapping for controllers (e.g., /api/data)
                endpoints.MapControllers();
            });
        }
    }
}
