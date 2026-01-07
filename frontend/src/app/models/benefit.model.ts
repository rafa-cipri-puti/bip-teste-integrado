export interface Benefit {
    id: number;
    nome: string;
    descricao: string;
    valor: number;
    ativo: boolean;
}

export interface User {
    id: number;
    nome: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface TransferRequest {
    fromId: number;
    toId: number;
    amount: number;
}

export interface TransferResponse {
    fromBeneficio: Benefit;
    toBeneficio: Benefit;
}