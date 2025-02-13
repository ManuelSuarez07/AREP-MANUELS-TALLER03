document.addEventListener('DOMContentLoaded', function() {
    const currencies = [
        { code: "ARS", name: "Argentina" },
        { code: "AUD", name: "Australia" },
        { code: "BRL", name: "Brazil" },
        { code: "CAD", name: "Canada" },
        { code: "CHF", name: "Switzerland" },
        { code: "CNY", name: "China" },
        { code: "COP", name: "Colombia" },
        { code: "EUR", name: "Eurozone" },
        { code: "GBP", name: "United Kingdom" },
        { code: "IDR", name: "Indonesia" },
        { code: "INR", name: "India" },
        { code: "JPY", name: "Japan" },
        { code: "KRW", name: "South Korea" },
        { code: "MXN", name: "Mexico" },
        { code: "MYR", name: "Malaysia" },
        { code: "NZD", name: "New Zealand" },
        { code: "RUB", name: "Russia" },
        { code: "SAR", name: "Saudi Arabia" },
        { code: "SEK", name: "Sweden" },
        { code: "SGD", name: "Singapore" },
        { code: "TRY", name: "Turkey" },
        { code: "USD", name: "United States" },
        { code: "ZAR", name: "South Africa" }
    ];

    function fillCurrencySelect(selectId) {
        const selectElement = document.getElementById(selectId);
        currencies.forEach(currency => {
            const option = document.createElement('option');
            option.value = currency.code;
            option.textContent = `${currency.code} - ${currency.name}`;
            selectElement.appendChild(option);
        });
    }

    fillCurrencySelect('fromCurrency');
    fillCurrencySelect('toCurrency');

    document.getElementById('converterForm').addEventListener('submit', function(event) {
        event.preventDefault();

        const amount = document.getElementById('amount').value;
        const fromCurrency = document.getElementById('fromCurrency').value.toLowerCase(); // Convertir a minúsculas
        const toCurrency = document.getElementById('toCurrency').value.toLowerCase(); // Convertir a minúsculas

        // Cambia la URL a la forma completa
        const url = `http://localhost:35000/convertir?amount=${amount}&fromCurrency=${fromCurrency}&toCurrency=${toCurrency}`;

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                document.getElementById('result').classList.remove('hidden');
                // Mostrar el resultado en mayúsculas
                document.getElementById('convertedAmount').textContent = 
                    `${data.amount} ${data.fromCurrency.toUpperCase()} = ${data.converted} ${data.toCurrency.toUpperCase()}`;
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ocurrió un error al realizar la conversión.');
            });
    });

    document.getElementById('swapCurrenciesBtn').addEventListener('click', function() {
        const fromCurrency = document.getElementById('fromCurrency');
        const toCurrency = document.getElementById('toCurrency');

        const temp = fromCurrency.value;
        fromCurrency.value = toCurrency.value;
        toCurrency.value = temp;
    });
});
