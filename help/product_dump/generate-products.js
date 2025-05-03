const fs = require("fs");
const { faker } = require("@faker-js/faker");
const createCsvWriter = require("csv-writer").createObjectCsvWriter;

const csvWriter = createCsvWriter({
  path: "products_10000.csv",
  header: [
    { id: "name", title: "name" },
    { id: "description", title: "description" },
    { id: "category", title: "category" },
    { id: "price", title: "price" },
    { id: "stock", title: "stock" },
    { id: "sku", title: "sku" },
    { id: "active", title: "active" },
    { id: "created_at", title: "created_at" },
    { id: "updated_at", title: "updated_at" },
  ],
});

const categories = ["Electronics", "Books", "Clothing", "Toys", "Food"];
const recordCount = 100_000;//1_000_000;
const batchSize = 10_000;

(async () => {
  console.log("Generating product data...");
  for (let i = 0; i < recordCount; i += batchSize) {
    const records = [];
    for (let j = 0; j < batchSize && i + j < recordCount; j++) {
      const now = new Date().toISOString();
      records.push({
        name: faker.commerce.productName(),
        description: faker.commerce.productDescription(),
        category: faker.helpers.arrayElement(categories),
        price: faker.number.float({ min: 10, max: 1000, precision: 0.01 }),
        stock: faker.number.int({ min: 0, max: 500 }),
        sku: faker.string.alphanumeric(10).toUpperCase(),
        active: faker.datatype.boolean(),
        created_at: now,
        updated_at: now,
      });
    }
    await csvWriter.writeRecords(records);
    console.log(`Written ${i + batchSize} records...`);
  }

  console.log("✅ Finished generating 1 million records in products.csv");
})();
