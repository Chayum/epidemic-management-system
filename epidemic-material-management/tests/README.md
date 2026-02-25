# Unit Tests

To run the tests, you need to install the following dependencies:

```bash
npm install -D vitest @vue/test-utils jsdom
```

## Running Tests

Add the following script to your `package.json`:

```json
"scripts": {
  "test": "vitest"
}
```

Then run:

```bash
npm run test
```

## Test Files

- `tests/unit/api/stats.spec.js`: Tests for Dashboard Stats API
- `tests/unit/api/material.spec.js`: Tests for Material API
