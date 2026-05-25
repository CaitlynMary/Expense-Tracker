export interface User {
  id: number;
  email: string;
  name: string;
  role?: string;
}

export interface AuthResponse {
  success?: boolean;
  message?: string;
  token: string;
  type?: string;
  id: number;
  email: string;
  name: string;
  user?: User;
}

export interface LoginRequest {
  email: string;
  password?: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password?: string;
}
