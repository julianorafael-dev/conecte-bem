// Executado antes de cada arquivo de teste do Jest.
import { TextDecoder, TextEncoder } from 'node:util';
import '@testing-library/jest-dom'; // matchers como toBeInTheDocument

// O jsdom não fornece TextEncoder/TextDecoder, exigidos pelo React Router 7.
globalThis.TextEncoder ??= TextEncoder;
globalThis.TextDecoder ??= TextDecoder;
