module.exports = {
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testEnvironment: 'jsdom',
  testMatch: ['**/*.spec.ts'],
  moduleNameMapper: {
    '^src/(.*)': '<rootDir>/src/$1',
    '^@app/(.*)': '<rootDir>/src/app/$1',
    '^@services/(.*)': '<rootDir>/src/app/services/$1',
    '^@models/(.*)': '<rootDir>/src/app/models/$1',
    '^@shared/(.*)': '<rootDir>/src/app/shared/$1',
    '^@layout/(.*)': '<rootDir>/src/app/layout/$1',
    '^@config/(.*)': '<rootDir>/src/app/config/$1',
    '^@environments/(.*)': '<rootDir>/src/environments/$1'
  },
  transformIgnorePatterns: ['node_modules/(?!@angular)']
};