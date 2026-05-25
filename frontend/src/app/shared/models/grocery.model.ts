export interface GroceryItem {
  id?: number;
  name: string;
  quantity: number;
  unit: string;
  estimatedPrice: number;
  isPurchased?: boolean;
  status?: 'PENDING' | 'PURCHASED';
  purchasedAt?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}
